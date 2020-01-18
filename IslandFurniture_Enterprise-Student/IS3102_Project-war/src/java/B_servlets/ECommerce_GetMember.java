package B_servlets;

import HelperClasses.Member;
import java.io.IOException;
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

@WebServlet(name = "ECommerce_GetMember", urlPatterns = {"/ECommerce_GetMember"})
public class ECommerce_GetMember extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        HttpSession session = request.getSession();
        String memberEmail = (String) session.getAttribute("memberEmail");
        
        Member member = retrieveMemberDetailsRESTful(memberEmail);
        session.setAttribute("member", member);
        
        String memberName = member.getName();
        session.setAttribute("memberName", memberName);
        
        // If redircted from ECommerce_MemberEditProfileServlet
        // For message display on memberProfile.jsp
        String bool = (String) session.getAttribute("bool");
        session.removeAttribute("bool");
        
        if (bool == "true"){
            response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp?goodMsg=Account%20updated%20successfully");
        }else if (bool == "false"){
            response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp?errMsg=Account%20failed%20to%20update");
        }else {
            response.sendRedirect("/IS3102_Project-war/B/SG/memberProfile.jsp");
        }
    }
    
    public Member retrieveMemberDetailsRESTful(String memberEmail){
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("http://localhost:8080/IS3102_WebService-Student/webresources/entity.memberentity")
                .path("getMemberDetails")
                .queryParam("memberEmail", memberEmail);
        Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON);
        invocationBuilder.header("some-header", "true");
        Response response = invocationBuilder.get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()){
            Member m = (Member) response.readEntity(Member.class);
            return m;
        }else {
            return null;
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
