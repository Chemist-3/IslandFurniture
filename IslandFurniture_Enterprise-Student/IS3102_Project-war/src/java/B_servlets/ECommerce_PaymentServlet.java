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

            double amountPaid = 0.0;
            for (ShoppingCartLineItem item : shoppingCart) {
                amountPaid += item.getPrice() * item.getQuantity();
            }
            String salesRecordID = createECommerceTransactionRecordRESTful(memberId, amountPaid, countryID);
            if (salesRecordID == null) {
                //error
                System.out.println("Error creating ECommerce Transaction Record. Sales record ID returned 0.");
                response.sendRedirect("/IS3102_Project-war/B/" + URLprefix + "shoppingCart.jsp?errMsg=Error processing transaction.");

                return;
            }
            for (ShoppingCartLineItem item : shoppingCart) {
                String itemID = item.getId();
                int quantity = item.getQuantity();
                //call ws to insert lineitem and salesrecordentity_lineitementity based on salesRecordID and lineItemID
                String result = createECommerceLineItemRecordRESTful(salesRecordID, itemID, quantity, countryID);
                if (result != null) {
                    System.out.println("createECommerceLineItemRecord successful");
                } else {
                    System.out.println("Error creating createECommerceLineItemRecord, returned null.");
                    response.sendRedirect("/IS3102_Project-war/B/" + URLprefix + "shoppingCart.jsp?errMsg=Error checking out.");
                    return;
                }
            }

            session.setAttribute("shoppingCart", null);

            response.sendRedirect("/IS3102_Project-war/B/" + URLprefix + "shoppingCart.jsp?goodMsg=Thank you for shopping at Island Furniture. You have checkout successfully!");
        } catch (Exception ex) {
            ex.printStackTrace();
            response.sendRedirect("/IS3102_Project-war/B/" + URLprefix + "shoppingCart.jsp?errMsg=Error checking out.");
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
        System.out.println("status: " + response.getStatus());

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
        System.out.println("status: " + response.getStatus());

        if (response.getStatus() != 201) {
            return null;
        }
        return "1";
        
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
