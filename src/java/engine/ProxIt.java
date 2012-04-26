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
import org.jsoup.Connection;
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
    	HashMap<String,String> hashLink = (HashMap<String,String>) servletContext.getAttribute("hashLink");
                 
            Integer counterLink = (Integer) servletContext.getAttribute("counterLink");
            
            System.out.println(request.getQueryString().replace("%3A%2F%2F", "://"));
            Document doc = Jsoup.connect(new URL(request.getQueryString().replace("%3A%2F%2F", "://").
                    substring(9)).toString()).userAgent("Mozilla").get();
           
            Elements resultLinks = doc.body().getElementsByTag("a");
            resultLinks.addAll(doc.body().getElementsByTag("img"));
             for(Element link : resultLinks)
            {
                //System.out.println(link.text()+" "+link.attr("abs:href"));
                String linkName = "id"+counterLink;
                hashLink.put(linkName, link.attr("abs:href"));
                link.attr("href", "Dispatch?link="+linkName);  //qui cambio gli attributi
               
                counterLink++;
            }
            
            
            Elements resultForm = doc.body().getElementsByTag("form");
            resultLinks.addAll(resultForm);
            for(Element link : resultForm)
            {
                if(link.attr("method").equalsIgnoreCase("POST"))
                {
                    System.out.println("VADO AL POST");
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
             
                }else{
                    
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
                }
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
           
    
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
       
            HashMap<String,String> hashLink = (HashMap<String,String>) servletContext.getAttribute("hashLink");
                 
            Integer counterLink = (Integer) servletContext.getAttribute("counterLink");
            
            
            Connection conn=Jsoup.connect(new URL(request.getQueryString().replace("%3A%2F%2F", "://").
                    substring(9)).toString()).userAgent("Mozilla");
            
            Cookie[] cookies=request.getCookies();
            for(int i=0;i<cookies.length;i++)
                conn.cookie(cookies[i].getName(),cookies[i].getValue());
            Enumeration<String> parametri=request.getParameterNames();
           while (parametri.hasMoreElements())
           {
               String tmp=parametri.nextElement();              
               conn.data(tmp, request.getParameter(tmp));
           }
           
           
            Document doc = conn.post();
           
            Elements resultLinks = doc.body().getElementsByTag("a");
            resultLinks.addAll(doc.body().getElementsByTag("img"));
            
            for(Element link : resultLinks)
            {
                //System.out.println(link.text()+" "+link.attr("abs:href"));
                String linkName = "id"+counterLink;
                hashLink.put(linkName, link.attr("abs:href"));
                link.attr("href", "Dispatch?link="+linkName);  //qui cambio gli attributi
               
                counterLink++;
            }
            
            Elements resultForm = doc.body().getElementsByTag("form");
            resultLinks.addAll(resultForm);
             
            for(Element link : resultForm)
            {
                if(link.attr("method").equalsIgnoreCase("POST"))
                {
                    System.out.println("VADO AL POST");
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
             
                }else{
                    
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
                }
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
            
    
    }
   
}