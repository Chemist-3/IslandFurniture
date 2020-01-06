<%@page import="HelperClasses.RetailProduct"%>
<%@page import="EntityManager.PromotionEntity"%>
<%@page import="EntityManager.Item_CountryEntity"%>
<%@page import="java.util.List"%>
<%@page import="java.text.DecimalFormat"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<jsp:include page="checkCountry.jsp" />
<html> <!--<![endif]-->
    <jsp:include page="header.html" />
    <body>
        <%
            List<RetailProduct> retailProducts = (List<RetailProduct>) (session.getAttribute("retailProducts"));
            try{
                System.out.println("retailProduct list size: " + retailProducts.size());
            }catch(Exception ex){
                // if nullpointerexception, list is null
                System.out.println("Exception in retailProduct: " + ex);
            }
        %>
        <div class="body">
            <jsp:include page="menu2.jsp" />
            <div class="body">
                <div role="main" class="main">
                    <section class="page-top">
                        <div class="container">
                            <div class="row">
                                <div class="col-md-12">
                                    <h2>Retail Products</h2>
                                </div>
                            </div>
                        </div>
                    </section>
                    <div class="container">
                        <div class="row">
                            <div class="col-md-6">
                                <h2 class="shorter"><strong>All Retail Products</strong></h2>
                            </div>
                        </div>
                        <div class="row">
                            <ul class="products product-thumb-info-list" data-plugin-masonry>
                                <%                                    
                                    try {
                                        // Loop through List to display all retail products
                                        for (int i = 0; retailProducts.size() > i; i++) {
                                %>
                                <li class="col-md-3 col-sm-6 col-xs-12 product">
                                    <span class="product-thumb-info">
                                        <span class="product-thumb-info-image">
                                            <img alt="" class="img-responsive" src="../../..<%=retailProducts.get(i).getImageUrl()%>">
                                        </span>
                                        <span class="product-thumb-info-content">
                                            <h4><%=retailProducts.get(i).getName()%></h4>
                                            <%
                                                // Proper format price for display
                                                DecimalFormat df = new DecimalFormat("#.00");
                                                String displayPrice = "$" + df.format(retailProducts.get(i).getPrice());
                                            %>
                                            <span class="product-thumb-info-act-left"><em>Price: <%=displayPrice%></em></span>
                                            <br/>
                                            <form action="retailProductDetails.jsp">
                                                <input type="hidden" name="sku" value="<%=retailProducts.get(i).getSKU()%>"/>
                                                <input type="submit" class="btn btn-primary btn-block" value="More Details"/>
                                            </form>
                                        </span>
                                    </span>
                                </li>
                                <%
                                        }
                                    } catch (Exception ex) {
                                        System.out.println(ex);
                                    }
                                %>
                            </ul>
                        </div>
                        <hr class="tall">
                    </div>
                </div>
            </div>
            <jsp:include page="footer.html" />
        </div>
    </body>
</html>
