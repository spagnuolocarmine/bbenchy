/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package engine;


import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.logging.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.net.URL;
import java.net.HttpURLConnection;
import java.util.HashMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class ProxIt extends HttpServlet {
        
	
	private ServletContext servletContext;
	private Logger log;
	
    public void init(ServletConfig servletConfig) throws ServletException {
    	servletContext = servletConfig.getServletContext();
        servletContext.setAttribute("hashLink",new HashMap<String, String>());
        servletContext.setAttribute("counterLink",new Integer(0));
  
        log = Logger.getLogger(ProxIt.class.getName());
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
       
            /*DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpHost targetHost = new HttpHost("www.google.it",80,"http");
            HttpHost proxy = new HttpHost("localhost",8080);
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

            HttpGet httpget = new HttpGet("/");
            System.out.println("\n\n----------------------------------------");
            System.out.println("executing request: " + httpget.getRequestLine());
            System.out.println("via proxy: " + proxy);
            System.out.println("to target: " + targetHost);

            HttpResponse response2 = httpclient.execute(targetHost, httpget);
            HttpEntity entity = response2.getEntity();

            System.out.println("----------------------------------------");
            System.out.println(response2.getStatusLine());
            if (entity != null) {
                System.out.println("Response content length: " + entity.getContentLength());
            */
             HashMap<String,String> hashLink = (HashMap<String,String>) servletContext.getAttribute("hashLink");
                 
            Integer counterLink = (Integer) servletContext.getAttribute("counterLink");
            //System.err.println("REQUEST: "+new URL(request.getQueryString()));
           
            
            
            
           /* String idSite=request.getParameter("link");
            String initial_url=new URL(request.getParameter("siteName")+request.getQueryString()).toString();
            String path_del="http://www.google.it/search?link=id1000siteName=";
            String url_val=initial_url.substring(path_del.length());
            System.out.println("pathdel "+ path_del+"RICHIESTA REALEMENTE FATTA PER :"+ url_val);
            
           */
            //Document doc = Jsoup.connect(url_val).userAgent("Mozilla").get();
            System.out.println(request.getQueryString().replace("%3A%2F%2F", "://"));
            Document doc = Jsoup.connect(new URL(request.getQueryString().replace("%3A%2F%2F", "://").substring(9)).toString()).userAgent("Mozilla").get();
           // Document doc = Jsoup.parse(new URL(url_val),0);
            
            
            
            
            Elements resultLinks = doc.body().getElementsByTag("a");
            resultLinks.addAll(doc.body().getElementsByTag("img"));
            
            for(Element link : resultLinks)
            {
                //System.out.println(link.text()+" "+link.attr("abs:href"));
                String linkName = "id"+counterLink;
                hashLink.put(linkName, link.attr("abs:href"));
                link.attr("href", "Dispatch?link="+linkName);  //qui cambio gli attributi
               // link.append("<input type=\"hidden\" value=\"id")
                counterLink++;
            }
            
            Elements resultForm = doc.body().getElementsByTag("form");
            resultLinks.addAll(resultForm);
             
            for(Element link : resultForm)
            {
                System.out.println("--------> ACTION:" +link.text()+" "+link.attr("abs:action")+" numero: "+counterLink);

                if(link.getElementById("proxitid")!=null)
                {
                    String linkName = link.getElementById("proxitid").attr("link");
                    hashLink.put(linkName, link.attr("abs:action"));
     
                }
                else
                {
                        String linkName = "id"+counterLink;
                        hashLink.put(linkName, link.attr("abs:action"));
                        link.append("<input id=\"proxitid\" type=\"hidden\" name=\"link\" value=\""+linkName+"\" />");
                        counterLink++;
                }
                
                
                
                System.out.println("questa è l'action ---> "+link.attr("abs:action"));
                link.attr("action", "Dispatch");  //qui cambio gli attributi
                
            
                //if(link.getElementById("proxitid")!=null)
                    //link.getElementById("proxitid").remove();
                    
                
                
               
            
                
            }
           
            doc.head().append(" <div class=\"forabg\">"+
            "<div class=\"inner\"><span class=\"corners-top\"><span></span></span>"+
               " <ul class=\"topiclist\">"+
                   " <li class=\"header\">"+
                   "     <dl class=\"icon\"\\>     <dt>SPAZIO PUBBLICITARIO DI {SITENAME}</dt>"+
                 "       </dl>"+
                "    </li>"+
               " </ul>"+
               " <ul class=\"topiclist forums\">"+
                   " <li>"+
                      "  <dl>"+
                     "       <dd style=\"padding:5px; text-align: center; border:none;\">"+
                    "            QUI METTI IL CODICE DELLA PUBBLICITA'"+
                   "         </dd>"+
                  "      </dl>"+
                 "   </li>"+
                "</ul>"+
                "<span class=\"corners-bottom\"><span></span></span></div></div>");
            response.getOutputStream().write(doc.html().getBytes());
            Cookie[] cookies=request.getCookies();
            for(int i=0;i<cookies.length;i++)
                response.addCookie(cookies[i]);
            response.addCookie(null);
    }
   
}