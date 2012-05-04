/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package engine;

import java.io.IOException;
import java.io.PrintWriter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;


/**
 *
 * @author rainmaker
 */
public class Dispatch extends HttpServlet {
    
     static public HashMap<String,String> hashLink ;
     static public Integer counterLink ;
	private ServletContext servletContext;
	private Logger log;
	
    public void init(ServletConfig servletConfig) throws ServletException {
    	servletContext = servletConfig.getServletContext();
             hashLink = (HashMap<String,String>) servletContext.getAttribute("hashLink");
             counterLink = (Integer) servletContext.getAttribute("counterLink");
        log = Logger.getLogger(ProxIt.class.getName());
    }

 

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            Cookie[] cookies=request.getCookies();
            for(int i=0;i<cookies.length;i++)
                response.addCookie(cookies[i]);
          
            Enumeration<String> parametri=request.getParameterNames();
           
            
            System.out.println("\n\nDISPATCH -> ricevuto: "+request.getParameter("link"));
            if(request.getQueryString().contains("&"))
            {
                response.sendRedirect("ProxIt?siteName="+hashLink.get(request.getParameter("link"))+"?"+request.getQueryString());
                System.out.println("DISPATCH \\w query -> redirect to: "+hashLink.get(request.getParameter("link"))+"?"+request.getQueryString());
            }
            else response.sendRedirect("ProxIt?siteName="+hashLink.get(request.getParameter("link")));
                
        } finally {            
            out.close();
        }
    }

 
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        executePost(request, response);
        
    }
    protected void executePost(HttpServletRequest request, HttpServletResponse response) throws MalformedURLException, IOException
    {
            HashMap<String,String> hashLink = (HashMap<String,String>) servletContext.getAttribute("hashLink");
                 
            Integer counterLink = (Integer) servletContext.getAttribute("counterLink");
            String site=hashLink.get(request.getParameter("link"));
            
            Connection conn=Jsoup.connect(new URL(site).toString()).userAgent("Mozilla");
           // conn.followRedirects(true);
            Cookie[] cookies=request.getCookies();
            for(int i=0;i<cookies.length;i++)
                conn.cookie(cookies[i].getName(),cookies[i].getValue());
            
           Enumeration<String> parametri=request.getParameterNames();
           while (parametri.hasMoreElements())
           {
               String tmp=parametri.nextElement();              
               conn.data(tmp, request.getParameter(tmp));
           }
           System.out.println("VADO NEL POST A "+new URL(site).toString());
         
            Document doc = conn.method(Connection.Method.POST).execute().parse();
           
            
                   for(Map.Entry<String,String> e: conn.response().cookies().entrySet())
                   
                       response.addCookie(new Cookie(e.getKey(), e.getValue()));
                   
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
            
            
            
            System.out.println(doc.head());
            System.out.println(doc.body());
            
            
            
            Elements resultScript= doc.body().getElementsByTag("script");
            resultScript.addAll(doc.head().getElementsByTag("script"));
           
            for(Element script : resultScript)
            {
                if(script.attr("type").equalsIgnoreCase("text/javascript")
                        &&
                        script.attr("language").equalsIgnoreCase("javascript"))
                {
                   // System.out.println("Elaborazione .... link in script:\n"+script.data());
                     String text_script=script.data();
                     System.out.println("vecchio testo: "+text_script);
                     String regeJsNoAbs="(\"|')(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|](\"|')";
                     Pattern pattern = Pattern.compile(regeJsNoAbs);
                     Matcher matcher = pattern.matcher(text_script);
                   
                      while(matcher.find())
                      {
                            String url_script=matcher.group();
                    
                            String linkName = "id"+counterLink;
                            counterLink++;
                            
                            
                            System.out.println("Provo a sostituire "+url_script+" con "+"Dispatch?link="+linkName);
                            
                            text_script=text_script.replaceAll(url_script,"'Dispatch?link="+linkName+"'");
                           
                            System.out.println("NUovo testo:\n"+text_script+"\n\n");
                            
                            script.
                                    
                                    text(text_script);
                            url_script=url_script.substring(1);
                            url_script=url_script.substring(0,url_script.length()-1);
                            hashLink.put(linkName, url_script);
                            
                            
                      }
                }
            }
            
            
            
            System.out.println(doc.head());
            System.out.println(doc.body());
            
            
            
            
            doc.head().append(" <div class=\"forabg\">"+
            "<div class=\"inner\"><span class=\"corners-top\"><span></span></span>"+
               " <ul class=\"topiclist\">"+
                   " <li class=\"header\">"+
                   "     <dl class=\"icon\"\\>     <dt>RISULTATI BENCHMARK</dt>"+
                 "       </dl>"+
                "    </li>"+
               " </ul>"+
               " <ul class=\"topiclist forums\">"+
                   " <li>"+
                      "  <dl>"+
                     "       <dd style=\"padding:5px; text-align: center; border:none;\">"+
                    "            TABELLA RISULTATI'"+
                   "         </dd>"+
                  "      </dl>"+
                 "   </li>"+
                "</ul>"+
                "<span class=\"corners-bottom\"><span></span></span></div></div>");
            
            
            response.getOutputStream().write(doc.html().getBytes());
            
    }

    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
