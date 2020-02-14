package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Date;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.dbaccess;

@Path("commerce")
public class ECommerceFacadeREST {

    @Context
    private UriInfo context;

    public ECommerceFacadeREST() {
    }

    private final String jbdc_path = "jdbc:mysql://localhost:3306/islandfurniture-it07?zeroDateTimeBehavior=convertToNull&user=root&password=12345";
    private final dbaccess db = new dbaccess();
    
    
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
    public Response createECommerceTransactionRecord(@QueryParam("memberId") Long memberID, 
            @QueryParam("amountPaid") Double amountPaid, @QueryParam("countryID") Long countryID) {
        
        System.out.println("RESTful: createECommerceTransactionRecord() called with memberID=" + memberID + "  amountPaid=" + amountPaid + " and countryID=" + countryID);
        
        try {
            // Get database connection obj
            Connection conn = DriverManager.getConnection(jbdc_path);
            // instantiate variables
            String currency = "";
            String storeID = "";
            java.util.Date dt = new java.util.Date();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(dt);
            
            // retrieve storeID and currency
            ResultSet rs = db.getCountryStoreIDAndCurrency(conn, countryID);
            while (rs.next()) {
                currency = rs.getString("currency");
                storeID = rs.getString("storeID");
            }
            // error checking
            if (currency.equals("") || storeID.equals("")) {
                conn.close();
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // create new entry in salesrecordentity and retrieve salesRecordID
            Long generatedKey = db.insertSalesrecordentity(conn, amountPaid, currentTime, currency, (new Date()).getTime(), memberID, Integer .parseInt(storeID));
            
            // error checking
            if (generatedKey > 0L) {
                conn.close();
                return Response.status(Response.Status.CREATED).entity(generatedKey + "").build();
            } else {
                conn.close();
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("createECommerceLineItemRecord")
    @Produces({"application/json"})
    public Response createECommerceLineItemRecord(@QueryParam("salesRecordID") String salesRecordID, @QueryParam("itemID") String itemID, 
            @QueryParam("quantity") int quantity, @QueryParam("countryID") Long countryID) {
        
        System.out.println("RESTful: createECommerceLineItemRecord() called with salesRecordID=" + salesRecordID + "  itemID=" + itemID + "quantity=" + quantity + "and countryID=" + countryID);
        
        try {
            // Get database connection obj
            Connection conn = DriverManager.getConnection(jbdc_path);
            
            // create new entry in lineitementity and retrieve lineItemID
            // linked with salesRecord to show which items are sold
            Long lineItemId = db.insertLineitementity(conn, quantity, itemID);
            
            // error checking
            if (lineItemId <= 0L) {
                conn.close();
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            
            // link salesRecord and itemRecord
            int result = db.insertSalesrecordentity_lineitementity(conn, salesRecordID, lineItemId);
            
            // error checking
            if (result == 0) {
                conn.close();
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // update existing item quantities and storage bin freevolume
            db.updateQuantity(conn, itemID, quantity, countryID);
            
            // return 1 for servlet error checking
            String responseResult = "1";
            
            conn.close();
            return Response.status(Response.Status.CREATED).entity(responseResult + "").build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
