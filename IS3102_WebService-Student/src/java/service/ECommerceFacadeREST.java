package service;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("commerce")
public class ECommerceFacadeREST {

    @Context
    private UriInfo context;

    public ECommerceFacadeREST() {
    }

    @GET
    @Produces("application/json")
    public String getJson() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    /**
     * PUT method for updating or creating an instance of ECommerce
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/json")
    public void putJson(String content) {
    }

    @GET
    @Path("createECommerceTransactionRecord")
    @Produces("application/json")
    public Response createECommerceTransactionRecord(@QueryParam("memberId") Long memberID, @QueryParam("amountPaid") Double amountPaid, @QueryParam("countryID") Long countryID) {
        try {
            String currency = "";
            String storeID = "";
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345");
            String stmt = "SELECT c.currency, s.ID as storeID FROM countryentity c,country_ecommerce ce, storeentity s where ce.WarehouseEntity_ID=s.WAREHOUSE_ID and c.ID=ce.CountryEntity_ID and c.ID=?;";
            PreparedStatement ps = conn.prepareStatement(stmt);
            ps.setLong(1, countryID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                currency = rs.getString("currency");
                storeID = rs.getString("storeID");
            }

            if (currency.equals("") || storeID.equals("")) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            java.util.Date dt = new java.util.Date();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(dt);
            System.out.println("memberID in WS: " + memberID);
            stmt = "INSERT INTO salesrecordentity (`AMOUNTDUE`, `AMOUNTPAID`, `AMOUNTPAIDUSINGPOINTS`, "
                    + "`CREATEDDATE`, `CURRENCY`, `LOYALTYPOINTSDEDUCTED`, `POSNAME`, `RECEIPTNO`, "
                    + "`SERVEDBYSTAFF`, `MEMBER_ID`, `STORE_ID`) "
                    + "VALUES ('" + amountPaid + "', '" + amountPaid + "', '0', '" + currentTime + "', '" + currency + "', "
                    + "'0', 'ECommerce', '" + (new Date()).getTime() + "', 'ECommerce', '" + memberID + "', '" + Integer.parseInt(storeID) + "')";

            ps = conn.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            System.out.println("getGeneratedKeys(): " + rs);
            Long generatedKey = 0L;
            while (rs.next()) {
                generatedKey = rs.getLong(1);
            }

            if (generatedKey > 0L) {
                return Response.status(Response.Status.CREATED).entity(generatedKey + "").build();
                //return Response.ok(generatedKey + "", MediaType.APPLICATION_JSON).status(Response.Status.CREATED).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("createECommerceLineItemRecord")
    @Produces({"application/json"})
    public Response createECommerceLineItemRecord(@QueryParam("salesRecordID") String salesRecordID, @QueryParam("itemID") String itemID, @QueryParam("quantity") int quantity, @QueryParam("countryID") Long countryID) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345");
            String stmt = "INSERT INTO lineitementity (`QUANTITY`, `ITEM_ID`) VALUES (?, ?);";
            PreparedStatement ps = conn.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, quantity);
            ps.setLong(2, Long.parseLong(itemID));
            System.out.println("execute update return: " + ps.executeUpdate());
            ResultSet rs = ps.getGeneratedKeys();
            Long lineItemId = 0L;
            while (rs.next()) {
                lineItemId = rs.getLong(1);
                System.out.println("generated key is " + lineItemId);
            }

            stmt = "INSERT INTO salesrecordentity_lineitementity (`SalesRecordEntity_ID`, `itemsPurchased_ID`) VALUES (?, ?);";
            ps = conn.prepareStatement(stmt);
            ps.setLong(1, Long.parseLong(salesRecordID));
            ps.setLong(2, lineItemId);
            int result = ps.executeUpdate();

            if (result == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            //update quantity of items
            //retrieve the warehouse id according to countryID
            stmt = "SELECT WarehouseEntity_ID FROM country_ecommerce where CountryEntity_ID =?;";
            ps = conn.prepareStatement(stmt);
            ps.setLong(1, countryID);
            rs = ps.executeQuery();
            
            Long warehouseID = 0L;
            while (rs.next()) {
                warehouseID = rs.getLong(1);
            }

            //retrieve lineitem ID and the quantities according to itemID
            stmt = "select l.ID, l.QUANTITY, s.ID as storagebinID, i.VOLUME "
                    + "from warehouseentity w, storagebinentity s, "
                    + "storagebinentity_lineitementity sl, lineitementity l, itementity i "
                    + "where i.id=l.ITEM_ID and sl.lineItems_ID=l.ID and s.ID=sl.StorageBinEntity_ID "
                    + "and w.ID=s.WAREHOUSE_ID and w.ID=? and l.ITEM_ID=?";
            ps = conn.prepareStatement(stmt);
            ps.setLong(1, warehouseID);
            ps.setLong(2, Long.parseLong(itemID));
            rs = ps.executeQuery();

            while (rs.next()) {
                Long lineItemID = rs.getLong(1);
                int qtyRemaining = rs.getInt(2);
                Long storageBinID = rs.getLong(3);
                int itemVolume = rs.getInt(4);
                if (quantity <= 0) {
                    break;
                }
                if (qtyRemaining - quantity >= 0) {
                    System.out.println("Quantity fufilled.");
                    String updateStmt = "UPDATE lineitementity SET QUANTITY = QUANTITY-? WHERE ID = ?";
                    ps = conn.prepareStatement(updateStmt);
                    ps.setLong(1, quantity);
                    ps.setLong(2, lineItemID);
                    ps.executeUpdate();

                    updateStmt = "UPDATE storagebinentity SET FREEVOLUME = FREEVOLUME+? WHERE ID = ?";
                    ps = conn.prepareStatement(updateStmt);
                    ps.setLong(1, quantity * itemVolume);
                    ps.setLong(2, storageBinID);
                    ps.executeUpdate();

                    quantity = 0;
                    break;
                } else {
                    System.out.println("Not deducted fully in bin");
                    quantity -= qtyRemaining;
                    String updateStmt = "UPDATE lineitementity SET QUANTITY = 0 WHERE ID = ?";
                    ps = conn.prepareStatement(updateStmt);
                    ps.setLong(1, lineItemID);
                    ps.executeUpdate();
                    System.out.println("quantity still needed: " + quantity);

                    updateStmt = "UPDATE storagebinentity SET FREEVOLUME = VOLUME WHERE ID = ?";
                    ps = conn.prepareStatement(updateStmt);
                    ps.setLong(1, storageBinID);
                    ps.executeUpdate();
                }
            }
            String suckcess = "1";
            return Response.status(Response.Status.CREATED).entity(suckcess + "").build();
            //return Response.ok(generatedKey + "", MediaType.APPLICATION_JSON).status(Response.Status.CREATED).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
