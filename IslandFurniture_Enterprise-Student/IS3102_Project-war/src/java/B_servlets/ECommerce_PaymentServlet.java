package B_servlets;

import HelperClasses.Member;
import HelperClasses.ShoppingCartLineItem;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@WebServlet(name = "ECommerce_PaymentServlet", urlPatterns = {"/ECommerce_PaymentServlet"})
public class ECommerce_PaymentServlet extends HttpServlet {

    private String URLprefix = "";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try {
            // retrieve session attributes
            HttpSession session = request.getSession();
            
            URLprefix = (String) session.getAttribute("URLprefix");
            if (URLprefix == null) {
                response.sendRedirect("/IS3102_Project-war/B/selectCountry.jsp");
                return;
            }
            
            Long countryID = (Long) session.getAttribute("countryID");
            Member member = (Member) session.getAttribute("member");
            Long memberId = member.getId();
            ArrayList<ShoppingCartLineItem> shoppingCart = (ArrayList<ShoppingCartLineItem>) session.getAttribute("shoppingCart");

            // price calculation for salesRecord
            double amountPaid = 0.0;
            for (ShoppingCartLineItem item : shoppingCart) {
                amountPaid += item.getPrice() * item.getQuantity();
            }

            // call ws to insert new salesRecord
            String salesRecordID = createECommerceTransactionRecordRESTful(memberId, amountPaid, countryID);

            // error checking
            if (salesRecordID == null) {
                System.out.println("Error creating ECommerce Transaction Record. Sales record ID returned 0.");
                response.sendRedirect("/IS3102_Project-war/B/" + URLprefix + "shoppingCart.jsp?errMsg=Error processing transaction. Please contact us at (65) 6475-7890");
            }

            // LOOP - Check for item quantities again
            for (ShoppingCartLineItem item : shoppingCart) {
                // call ws to retrieve and check item quantity
                int quantity = checkQuantityRESTful(countryID, item.getSKU());
                
                //error checking
                if (quantity < 1) {
                    response.sendRedirect("/IS3102_Project-war/B/" + URLprefix + "shoppingCart.jsp?errMsg=Error checking out. Not enough quantity available. <br> Please contact us at (65) 6475-7890");
                }
            } // end of loop
            
            
            // LOOP - Create lineItem records and update quantity for every unique items in the Shopping Cart
            for (ShoppingCartLineItem item : shoppingCart) {
                String itemID = item.getId();
                int quantity = item.getQuantity();

                // call ws to insert lineitem and salesrecordentity_lineitementity based on salesRecordID and lineItemID
                // and update item quantity and storage bin freevolume
                String result = createECommerceLineItemRecordRESTful(salesRecordID, itemID, quantity, countryID);

                // error checking
                if (result != null) {
                    System.out.println("createECommerceLineItemRecord successful");
                } else {
                    System.out.println("Error creating createECommerceLineItemRecord, returned null.");
                    response.sendRedirect("/IS3102_Project-war/B/" + URLprefix + "shoppingCart.jsp?errMsg=Error checking out. Please contact us at (65) 6475-7890");
                }
            } // end of loop

            // clear shopping cart as items previously are paid for
            session.setAttribute("shoppingCart", null);
            
            // call ws to retrieve store infomation using salesRecordID
            String storeInfomationString = retrieveStoreInfomationRESTful(salesRecordID);
            if (storeInfomationString == null){
                response.sendRedirect("/IS3102_Project-war/B/" + URLprefix + "shoppingCart.jsp?goodMsg=Checkout succcessful, thank you for shopping at Island Furniture!");
            }
            // redirect to shoppingCart.jsp with success message and store collection infomation
            response.sendRedirect("/IS3102_Project-war/B/" + URLprefix + "shoppingCart.jsp?goodMsg=Checkout succcessful, thank you for shopping at Island Furniture! "
                    + " <br> Please collect your items at: " + storeInfomationString);
        } catch (Exception ex) {
            ex.printStackTrace();
            response.sendRedirect("/IS3102_Project-war/B/" + URLprefix + "shoppingCart.jsp?errMsg=Error checking out. Please contact us at (65) 6475-7890");
        }
    }

    protected String createECommerceTransactionRecordRESTful(Long memberId, double amountPaid, Long countryID) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/commerce")
                .path("createECommerceTransactionRecord")
                .queryParam("memberId", memberId)
                .queryParam("amountPaid", amountPaid)
                .queryParam("countryID", countryID);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        System.out.println("createECommerceTransactionRecordRESTful() status: " + response.getStatus());

        if (response.getStatus() != 201) {
            return null;
        }
        String salesRecordID = response.readEntity(String.class);
        return salesRecordID;
    }

    protected String createECommerceLineItemRecordRESTful(String salesRecordID, String itemID, int quantity, Long countryID) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/commerce")
                .path("createECommerceLineItemRecord")
                .queryParam("salesRecordID", salesRecordID)
                .queryParam("itemID", itemID)
                .queryParam("quantity", quantity)
                .queryParam("countryID", countryID);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        System.out.println("createECommerceLineItemRecordRESTful() status: " + response.getStatus());

        if (response.getStatus() != 201) {
            return null;
        }
        String result = response.readEntity(String.class);
        return result;
    }

        public int checkQuantityRESTful(Long countryID, String SKU) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.countryentity")
                .path("getQuantity")
                .queryParam("countryID", countryID)
                .queryParam("SKU", SKU);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("some-header", "true");
        Response response = invocationBuilder.get();

        if (response.getStatus() != 200) {
            return 0;
        }

        String quantity = (String) response.readEntity(String.class);
        return Integer.parseInt(quantity);

    }
        
    protected String retrieveStoreInfomationRESTful(String salesRecordID) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.storeentity")
                .path("getStoreInfomation")
                .queryParam("salesRecordID", salesRecordID);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        System.out.println("retrieveStoreInfomationRESTful() status: " + response.getStatus());

        if (response.getStatus() != 200) {
            return null;
        }
        String result = response.readEntity(String.class);
        return result;
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}