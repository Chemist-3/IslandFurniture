package service;

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

    private dbaccess db = new dbaccess();
    
    
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
            
            String currency = "";
            String storeID = "";
            java.util.Date dt = new java.util.Date();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(dt);
            
            
            ResultSet rs = db.getCountryStoreIDAndCurrency(countryID);
            while (rs.next()) {
                currency = rs.getString("currency");
                storeID = rs.getString("storeID");
            }
            if (currency.equals("") || storeID.equals("")) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            Long generatedKey = db.insertSalesrecordentity(amountPaid, currentTime, currency, (new Date()).getTime(), memberID, Integer .parseInt(storeID));
            
            if (generatedKey > 0L) {
                return Response.status(Response.Status.CREATED).entity(generatedKey + "").build();
            } else {
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
            Long lineItemId = db.insertLineitementity(quantity, itemID);
            if (lineItemId <= 0L) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            int result = db.insertSalesrecordentity_lineitementity(salesRecordID, lineItemId);
            if (result == 0) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            db.updateQuantity(itemID, quantity, countryID);
            
            String responseResult = "1";
            return Response.status(Response.Status.CREATED).entity(responseResult + "").build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
