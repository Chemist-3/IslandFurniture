package B_servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@WebServlet(name = "ECommerce_MemberEditProfileServlet", urlPatterns = {"/ECommerce_MemberEditProfileServlet"})
public class ECommerce_MemberEditProfileServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        HttpSession session = request.getSession();

        String email = (String) session.getAttribute("memberEmail");
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        Integer securityQuestion = Integer.parseInt(request.getParameter("securityQuestion"));
        String securityAnswer = request.getParameter("securityAnswer");
        Integer age = Integer.parseInt(request.getParameter("age"));
        Integer income = Integer.parseInt(request.getParameter("income"));
        
        boolean outcome;
        if (request.getParameter("password") != null && !request.getParameter("password").isEmpty()){
            String password = request.getParameter("password");
            outcome = updateMemberDetailsPwRESTful(name, email, phone, address, securityQuestion, securityAnswer, age, income, password);
        }else{
            outcome = updateMemberDetailsRESTful(name, email, phone, address, securityQuestion, securityAnswer, age, income);
        }

        // For message display on memberProfile.jsp
        if(outcome){
            session.setAttribute("bool", "true");
        }else{
            session.setAttribute("bool", "false");
        }
        
        response.sendRedirect("ECommerce_GetMemberServlet");
        
    }
    
     public boolean updateMemberDetailsRESTful(String name, String email, String phone, String address, Integer securityQuestion, String securityAnswer, Integer age, Integer income){
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.memberentity")
                .path("updateMemberDetails")
                .queryParam("name", name)
                .queryParam("email", email)
                .queryParam("phone", phone)
                .queryParam("address", address)
                .queryParam("securityQuestion", securityQuestion)
                .queryParam("securityAnswer", securityAnswer)
                .queryParam("age", age)
                .queryParam("income", income);
        
        Invocation.Builder invocationBuilder = target.request();
        invocationBuilder.header("some-header", "true");
        Response response = invocationBuilder.put(Entity.entity("","application/json" ));
        System.out.println("status: " + response.getStatus());

        if (response.getStatus() == Response.Status.OK.getStatusCode()){
            return true;
        }else {
            return false;
        } 
    }
     
     public boolean updateMemberDetailsPwRESTful(String name, String email, String phone, String address, Integer securityQuestion, String securityAnswer, Integer age, Integer income, String password){
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.memberentity")
                .path("updateMemberDetailsPw")
                .queryParam("name", name)
                .queryParam("email", email)
                .queryParam("phone", phone)
                .queryParam("address", address)
                .queryParam("securityQuestion", securityQuestion)
                .queryParam("securityAnswer", securityAnswer)
                .queryParam("age", age)
                .queryParam("income", income)
                .queryParam("password", password);
        
        Invocation.Builder invocationBuilder = target.request();
        invocationBuilder.header("some-header", "true");
        Response response = invocationBuilder.put(Entity.entity("","application/json" ));
        System.out.println("status: " + response.getStatus());

        if (response.getStatus() == Response.Status.OK.getStatusCode()){
            return true;
        }else {
            return false;
        }
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
