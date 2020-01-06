package B_servlets;

import HelperClasses.RetailProduct;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ECommerce_AllRetailProductsServlet extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {

            HttpSession session = request.getSession();
            Long countryID = (Long) session.getAttribute("countryID");
            
            String category;
            List<RetailProduct> retailProducts;
            
            // Check for null from request.getParameter to avoid NPE
            if (request.getParameter("cat") == null){
                category = "";
                retailProducts = getRetailProductListRESTful(countryID);
            }else{
                category = URLDecoder.decode(request.getParameter("cat"));
                retailProducts = getRetailProductListRESTful(countryID, category);
            }
            
            session.setAttribute("retailProducts", retailProducts);
            String categoryEncoded = URLEncoder.encode(category);
            
            if (category.isEmpty()){
                response.sendRedirect("/IS3102_Project-war/B/SG/allRetailProducts.jsp");
                return;
            } else if (retailProducts == null) {
                response.sendRedirect("/IS3102_Project-war/B/SG/retailProductCategory.jsp?cat=" + categoryEncoded + "&errorMsg=No retail product to be displayed.");
                return;
            }
            response.sendRedirect("/IS3102_Project-war/B/SG/retailProductCategory.jsp?cat=" + categoryEncoded);
        } catch (Exception ex) {
            out.println("AllRetailProductServlet Exception: \n\n " + ex.toString());
            
        }
    }

    public List<RetailProduct> getRetailProductListRESTful(Long countryID) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                //.target("http://localhost:8080/IS3102_WebService/webresources/entity.retailproductentity")
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.retailproductentity")
                .path("getRetailProductList")
                .queryParam("countryID", countryID);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("some-header", "true");
        Response response = invocationBuilder.get();
        System.out.println("status: " + response.getStatus());

        if (response.getStatus() != 200) {
            return null;
        }

        List<RetailProduct> list = response.readEntity(new GenericType<List<RetailProduct>>() {
        });

        return list;
    }
    
        public List<RetailProduct> getRetailProductListRESTful(Long countryID, String category) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                //.target("http://localhost:8080/IS3102_WebService/webresources/entity.retailproductentity")
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.retailproductentity")
                .path("getRetailProductListByCat")
                .queryParam("countryID", countryID)
                .queryParam("category", category);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("some-header", "true");
        Response response = invocationBuilder.get();
        System.out.println("status: " + response.getStatus());

        if (response.getStatus() != 200) {
            return null;
        }

        List<RetailProduct> list = response.readEntity(new GenericType<List<RetailProduct>>() {
        });
        System.out.println("list size: " + list.size());

        return list;
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