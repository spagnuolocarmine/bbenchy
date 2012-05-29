/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package engine;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java_cup.runtime.Symbol;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author f.milone, carspa, frarai
 */
public class BenchIt extends HttpServlet {

    private ServletContext servletContext;
    private Logger log;
    
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        servletContext = servletConfig.getServletContext();
        servletContext.setAttribute("hashLink",new HashMap<String, String>());
        servletContext.setAttribute("counterLink",new Integer(0));
        log = Logger.getLogger(ProxIt.class.getName());
    }
    
     /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HashMap<String,String> hashLink = (HashMap<String,String>) servletContext.getAttribute("hashLink");
        Integer counterLink = (Integer) servletContext.getAttribute("counterLink");
        //compute the next hop
        String nextPage="";
        if(request.getParameter("link")!=null) nextPage=hashLink.get(request.getParameter("link"));
        else nextPage=request.getQueryString().replaceAll("%3A", ":").replaceAll("%2F", "/").substring(9);
        //set up connection
        Connection conn=Jsoup.connect(new URL(nextPage).toString());
        //set up cookies
            Cookie[] cookies=request.getCookies();
                if(cookies!=null)
                for(int i=0;i<cookies.length;i++)
                    conn.cookie(cookies[i].getName(),cookies[i].getValue());
        //set up all HTTP protocol headers
            setHTTPheader(request, conn, nextPage);
        //set up all parameter for a POST connection
            Enumeration<String> parameter=request.getParameterNames(); 
            while (parameter.hasMoreElements())
            {
                String tmp=parameter.nextElement();  
                conn.data(tmp, request.getParameter(tmp));
            }
        //do connection with method POST
            long start_timer=System.currentTimeMillis();
              Connection.Response resp_conn=conn.method(Connection.Method.POST).execute();
            long end_timer=System.currentTimeMillis();
            long time=end_timer-start_timer;
         //get the document
         Document doc = resp_conn.parse();
         
         //set up response attributes "TO ASK...NON DETERMINISTIC" -> l'accesso ad amazon fallisce se decommento <- 
         //setHTTPheader(request, response, nextPage);
         
         //set cookies in response object
         for(Map.Entry<String,String> e: conn.response().cookies().entrySet()) response.addCookie(new Cookie(e.getKey(), e.getValue()));
           
         replaceLinks(hashLink, counterLink, doc);
         
            
         //Prepare document for parsing
         StringReader sr = new StringReader(doc.html());
         JSParser jsParser = new JSParser(sr);
         //parse JavaScript
            jsParser.set_request_time("POST "+time+"ms");
            Symbol s; while ((s = jsParser.next_token()).sym != -1999);
         //write the modified document to browser
         response.getOutputStream().write(jsParser.getModFile().getBytes());
         
      
        /* 
         replaceJavaScriptLinks(doc, hashLink, counterLink);
         response.getOutputStream().write(doc.html().getBytes());
         */
         
         
    }

    
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       
        HashMap<String,String> hashLink = (HashMap<String,String>) servletContext.getAttribute("hashLink");
        Integer counterLink = (Integer) servletContext.getAttribute("counterLink");
        //compute the next hop
        String nextPage="";
        if(request.getParameter("link")!=null) nextPage=hashLink.get(request.getParameter("link"));
        else nextPage=request.getQueryString().replaceAll("%3A", ":").replaceAll("%2F", "/").substring(9);
        System.out.println("GET -> "+nextPage);
        //set up connection
        Connection conn=Jsoup.connect(new URL(nextPage).toString());
        //set up cookies
            Cookie[] cookies=request.getCookies();
                if(cookies!=null)
                for(int i=0;i<cookies.length;i++)
                    conn.cookie(cookies[i].getName(),cookies[i].getValue());
        //set up all parameter for a POST connection
            if(request.getQueryString().contains("&"))
            { 
                    Enumeration<String> parameter=request.getParameterNames();
                
                    while (parameter.hasMoreElements())
                    {
                        String tmp=parameter.nextElement();  
                        conn.data(tmp, request.getParameter(tmp));
                    }
             }
        //set up all HTTP protocol headers
            setHTTPheader(request, conn, nextPage);
        //do connection with method GET
            long start_timer=System.currentTimeMillis();
              Connection.Response resp_conn=conn.method(Connection.Method.GET).execute();
            long end_timer=System.currentTimeMillis();
            long time=end_timer-start_timer;
         //get the document
         Document doc = resp_conn.parse();
         
         //set up response attributes
         setHTTPheader(request, response, nextPage);
         //set cookies in response object
         for(Map.Entry<String,String> e: conn.response().cookies().entrySet()) response.addCookie(new Cookie(e.getKey(), e.getValue()));
          
         replaceLinks(hashLink, counterLink, doc);
            
         //Prepare document for parsing
         StringReader sr = new StringReader(doc.html());
         JSParser jsParser = new JSParser(sr);
         //parse JavaScript
            jsParser.set_request_time("GET "+time+"ms");
            Symbol s; while ((s = jsParser.next_token()).sym != -1999);
         //write the modified document to browser
         response.getOutputStream().write(jsParser.getModFile().getBytes());
         
      
        /* 
         replaceJavaScriptLinks(doc, hashLink, counterLink);
         response.getOutputStream().write(doc.html().getBytes());
         */
            
    
    }
    
    public void setHTTPheader(HttpServletRequest request,Connection conn, String url)
    {
           Enumeration<String> en=request.getHeaderNames();
           while(en.hasMoreElements()) 
           { 
               String curr_he=en.nextElement();
               
               if(!curr_he.equals("referrer"))
               conn.header(curr_he, request.getHeader(curr_he));

           }
           if(request.getQueryString()!=null)
           conn.referrer(url);
 
    }
    
    public void setHTTPheader(HttpServletRequest request,HttpServletResponse conn, String url)
    {
           Enumeration<String> en=request.getHeaderNames();
           while(en.hasMoreElements()) 
           { 
               String curr_he=en.nextElement();
               
               if(curr_he.equals("referrer")) conn.setHeader(curr_he, url);
               else conn.setHeader(curr_he, request.getHeader(curr_he));
           }
 
    }
    
    private void replaceJavaScriptLinks(Document doc, HashMap<String,String> hashLink, Integer counterLink)
    {
            Elements resultScript= doc.body().getElementsByTag("script");
            resultScript.addAll(doc.head().getElementsByTag("script"));
           
            for(Element script : resultScript)
            {
                if(script.attr("type").equalsIgnoreCase("text/javascript")
                        &&
                        script.attr("language").equalsIgnoreCase("javascript"))
                {
                     String text_script=script.data();
                     String regeJsNoAbs="(\"|')(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|](\"|')";
                     Pattern pattern = Pattern.compile(regeJsNoAbs);
                     Matcher matcher = pattern.matcher(text_script);
                   
                      while(matcher.find())
                      {
                            String url_script=matcher.group();
                    
                            String linkName = "id"+counterLink;
                            counterLink++;
                            text_script=text_script.replaceAll(url_script,"'BenchIt?link="+linkName+"'");
                            script.text(text_script);
                            url_script=url_script.substring(1);
                            url_script=url_script.substring(0,url_script.length()-1);
                            hashLink.put(linkName, url_script);
                            
                            
                      }
                }
            }
    }
    
    private void replaceLinks(HashMap<String,String> hashLink, Integer counterLink, Document doc){
        
        Elements resultLinks = doc.body().getElementsByTag("a");
          
             for(Element link : resultLinks)
            {
                String linkName = "id"+counterLink;
                hashLink.put(linkName, link.attr("abs:href"));
                link.attr("href", "BenchIt?link="+linkName);  
            
                counterLink++;
            }
            
             Elements resultLinksImg = doc.body().getElementsByTag("img");
             for(Element link : resultLinks)
                {
                    String val=link.attr("abs:src");
                    link.attr("src", val);  
            
            
                }
            
            Elements resultForm = doc.body().getElementsByTag("form");
            resultLinks.addAll(resultForm);
             
            for(Element link : resultForm)
            {
                if(link.attr("method").equalsIgnoreCase("POST"))
                {
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
                    link.attr("action", "BenchIt");
             
                }else{
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
                    link.attr("action", "BenchIt");  
                }
            }
    
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
