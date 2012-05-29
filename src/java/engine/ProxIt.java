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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java_cup.runtime.Symbol;
import javax.sound.midi.SysexMessage;
import org.apache.catalina.Session;
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
        private String myhead="<style type=\"text/css\"> "
                    + "body{font: 76%/1.3 Arial,Verdana,sans-serif}"
                    + "div.box{width:900px !important;width /**/:600px;"
                    + "height:190px !important;height /**/: 600px;"
                    + "overflow:auto;padding: 4px;"
                    + "border:1px solid #EEE;border-right:0 solid;"
                    + "background:url(images/gradient.png) repeat-x fixed top left}"
                    + "div.box p{margin-top:0}"
                    + "</style>"
                    + "<script language=\"JavaScript\" type=\"text/javascript\">"
                    + "function sleep(milliseconds) {"
                    + "var start = new Date().getTime();"
                    + "for (var i = 0; i < 1e7; i++) {"
                    + "if ((new Date().getTime() - start) > milliseconds){"
                    + "break;}}}"
                    + "function addItem(name, val) {"
                    + "var list = document.getElementById(\"list\");"
                    + "var newNode = document.createElement(\"li\");"
                    + "newNode.innerHTML=name+\" \"+val;"
                    + "list.insertBefore(newNode, list.firstChild);"
                    + "}</script>"
                   
                    + "<div class=\"box\">"
                 + "<h1>RISULTATI BBenchy</h1>"
                    + "<ul id=\"list\"> "
                    + "<!--------QUI LO SCRIPT INSERISCE I RISULTATI------->"
                    + "</ul>"
                    + "</div>";
	
    public void init(ServletConfig servletConfig) throws ServletException {
    	servletContext = servletConfig.getServletContext();
        servletContext.setAttribute("hashLink",new HashMap<String, String>());
        servletContext.setAttribute("counterLink",new Integer(0));
  
        log = Logger.getLogger(ProxIt.class.getName());
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	
        HashMap<String,String> hashLink = (HashMap<String,String>) servletContext.getAttribute("hashLink");
                 
            Integer counterLink = (Integer) servletContext.getAttribute("counterLink");
            System.out.println("ricevuta query string: "+request.getQueryString());
            //System.out.println(request.getQueryString().replaceAll("%3A%2F%2F", "://"));
            Connection conn=Jsoup.connect(new URL(request.getQueryString().replaceAll("%3A", ":").replaceAll("%2F", "/").
                    substring(9)).toString());
            conn.followRedirects(true);
            
            Cookie[] cookies=request.getCookies();
            if(cookies!=null)
            for(int i=0;i<cookies.length;i++)
                conn.cookie(cookies[i].getName(),cookies[i].getValue());
  
           //set HTTP header
            setHTTPheader(request,conn);
         
  
            long start_timer=System.currentTimeMillis();
              Connection.Response resp_conn=conn.userAgent(request.getHeader("user-agent")).method(Connection.Method.GET).execute();
            long end_timer=System.currentTimeMillis();
            long time=end_timer-start_timer;
           
            Document doc = resp_conn.parse();
           
            
           for(Map.Entry<String,String> e: conn.response().cookies().entrySet())
           {
               response.addCookie(new Cookie(e.getKey(), e.getValue()));
           }
            
            Elements resultLinks = doc.body().getElementsByTag("a");
          
             for(Element link : resultLinks)
            {
                //System.out.println(link.text()+" "+link.attr("abs:href"));
                String linkName = "id"+counterLink;
                hashLink.put(linkName, link.attr("abs:href"));
                link.attr("href", "Dispatch?link="+linkName);  //qui cambio gli attributi
            
                counterLink++;
            }
              Elements resultLinksImg = doc.body().getElementsByTag("img");
              for(Element link : resultLinks)
            {
                String val=link.attr("abs:src");
                link.attr("src", val);  //qui cambio gli attributi
            
            
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
           
            if(cookies!=null)
            for(int i=0;i<cookies.length;i++)
            {
                //System.out.println(cookies[i].getName()+ " "+cookies[i].getValue());
                response.addCookie(cookies[i]);
            }

             
              //send page to browser
                
             FileOutputStream fos = new FileOutputStream("temp.txt");
             PrintWriter pw = new PrintWriter(fos);
             pw.print(doc.html());
             pw.close();
             fos.close();
             FileReader fr = new FileReader("temp.txt");
             JSParser p = new JSParser(fr);   
             p.set_request_time(time+", POST");
             Symbol s;
		while ((s = p.next_token()).sym != -1999);
               
                 
             response.getOutputStream().write(p.getModFile().getBytes());
             
        
                //response.getOutputStream().write(doc.html().getBytes());
            
           
    
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
       
            HashMap<String,String> hashLink = (HashMap<String,String>) servletContext.getAttribute("hashLink");
                 
            Integer counterLink = (Integer) servletContext.getAttribute("counterLink");
            
            
            
             Connection conn=Jsoup.connect(new URL(request.getQueryString().replaceAll("%3A", ":").replaceAll("%2F", "/").
                    substring(9)).toString());
             conn.followRedirects(true);
           
            Cookie[] cookies=request.getCookies();
            for(int i=0;i<cookies.length;i++)
                conn.cookie(cookies[i].getName(),cookies[i].getValue());
            
            Enumeration<String> parametri=request.getParameterNames();
            
           while (parametri.hasMoreElements())
           {
               String tmp=parametri.nextElement();              
               conn.data(tmp, request.getParameter(tmp));
           }
           //conn.followRedirects(true);
           
           //set HTTP header
            setHTTPheader(request,conn);
            
             long start_timer=System.currentTimeMillis();
              Connection.Response resp_conn=conn.userAgent(request.getHeader("user-agent")).method(Connection.Method.POST).execute();
            long end_timer=System.currentTimeMillis();
            long time=end_timer-start_timer;
           
            Document doc = resp_conn.parse();
         
        
           for(Map.Entry<String,String> e: conn.response().cookies().entrySet())
               {
              // System.out.println("Inserisco cookie "+e.getKey());
               response.addCookie(new Cookie(e.getKey(), e.getValue()));
           }
         
             Elements resultLinks = doc.body().getElementsByTag("a");
          
             for(Element link : resultLinks)
            {
                //System.out.println(link.text()+" "+link.attr("abs:href"));
                String linkName = "id"+counterLink;
                hashLink.put(linkName, link.attr("abs:href"));
                link.attr("href", "Dispatch?link="+linkName);  //qui cambio gli attributi
            
                counterLink++;
            }
            
             Elements resultLinksImg = doc.body().getElementsByTag("img");
             for(Element link : resultLinks)
                {
                    String val=link.attr("abs:src");
                    link.attr("src", val);  //qui cambio gli attributi
            
            
                }
            
            Elements resultForm = doc.body().getElementsByTag("form");
            resultLinks.addAll(resultForm);
             
            for(Element link : resultForm)
            {
                if(link.attr("method").equalsIgnoreCase("POST"))
                {
                    //System.out.println("VADO AL POST");
                    //System.out.println("--------> ACTION:" +link.text()+" "+link.attr("abs:action")+" numero: "+counterLink);


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

                    //System.out.println("questa è l'action ---> "+link.attr("abs:action"));

                    link.attr("action", "Dispatch");  //qui cambio gli attributi
             
                }else{
                    
                     //System.out.println("--------> ACTION:" +link.text()+" "+link.attr("abs:action")+" numero: "+counterLink);


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

                    //System.out.println("questa è l'action ---> "+link.attr("abs:action"));

                    link.attr("action", "Dispatch");  //qui cambio gli attributi
                }
            }
            //<script type="text/javascript" language="javascript">
            
            Elements resultScript= doc.body().getElementsByTag("script");
            for(Element script : resultScript)
            {
                if(script.attr("type").equalsIgnoreCase("text/javascript")
                        &&
                        script.attr("language").equalsIgnoreCase("javascript"))
                {
                    System.out.println("Elaborazione .... link in script:");
                     String text_script=script.data();
                     String regeJsNoAbs="(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
                     Pattern pattern = Pattern.compile(regeJsNoAbs);
                     Matcher matcher = pattern.matcher(text_script);
                      while(matcher.find()) {
                        System.out.println("Sottosequenza : "+matcher.group());
                        System.out.println("Sottogruppo 1 : "+matcher.group(1));
                        }
                }
            }
            
                //set cookies in response message
                for(int i=0;i<cookies.length;i++)
                {
                    //System.out.println(cookies[i].getName()+ " "+cookies[i].getValue());
                    response.addCookie(cookies[i]);
                }

            //send page to browser
                
             FileOutputStream fos = new FileOutputStream("temp.txt");
             PrintWriter pw = new PrintWriter(fos);
             pw.print(doc.html());
             pw.close();
             fos.close();
             FileReader fr = new FileReader("temp.txt");
             JSParser p = new JSParser(fr);   
             p.set_request_time(time+", POST");
             Symbol s; while ((s = p.next_token()).sym != -1999);
             response.getOutputStream().write(p.getModFile().getBytes());
             
        
                //response.getOutputStream().write(doc.html().getBytes());
    
    }
    
    public void setHTTPheader(HttpServletRequest request,Connection conn)
    {
           Enumeration<String> en=request.getHeaderNames();
           while(en.hasMoreElements()) 
           { 
               String curr_he=en.nextElement();
               
               if(!curr_he.equals("referrer"))
               conn.header(curr_he, request.getHeader(curr_he));

           }
           if(request.getQueryString()!=null)
           conn.header("referer", request.getQueryString().replaceAll("%3A", ":").replaceAll("%2F", "/").
                    substring(9));
 
    }
   
}