/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package engine;

import java.io.FileOutputStream;
import java.io.FileReader;
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
import java_cup.runtime.Symbol;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;
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
        hashLink = (HashMap<String,String>) servletContext.getAttribute("hashLink");
        counterLink = (Integer) servletContext.getAttribute("counterLink");
        log = Logger.getLogger(ProxIt.class.getName());
    }

 

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       
        response.setContentType(request.getContentType());
        PrintWriter out = response.getWriter();
       
        try {

            Enumeration<String> parametri=request.getParameterNames();
           

            //Set cookies in response message
            Cookie[] cookies=request.getCookies();
            for(int i=0;i<cookies.length;i++)
            {
              //System.out.println(cookies[i].getName()+ " "+cookies[i].getValue());
                response.addCookie(cookies[i]);
            }

            
          
            
            //System.out.println("\n\nDISPATCH -> ricevuto: "+request.getParameter("link"));
            if(request.getQueryString().contains("&"))
            {
                response.sendRedirect("ProxIt?siteName="+hashLink.get(request.getParameter("link"))+"?"+request.getQueryString());
                //System.out.println("DISPATCH \\w query -> redirect to: "+hashLink.get(request.getParameter("link"))+"?"+request.getQueryString());
            }
            else 
                response.sendRedirect("ProxIt?siteName="+hashLink.get(request.getParameter("link")));
                
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
            
            Connection conn=Jsoup.connect(new URL(site).toString());
            conn.followRedirects(true);
            
            //set cookies for HTTP connection
            Cookie[] cookies=request.getCookies();
            for(int i=0;i<cookies.length;i++)
                conn.cookie(cookies[i].getName(),cookies[i].getValue());
           
            //set HTTP attribute 
             Enumeration<String> parametri=request.getParameterNames();
             while (parametri.hasMoreElements())
             {
                String tmp=parametri.nextElement(); 
                conn.data(tmp, request.getParameter(tmp));
             }
          // System.out.println("VADO NEL POST A "+new URL(site).toString());
         
           
             //set HTTP Header
             setHTTPheader(request, conn);
           
           
           //do HTTP connection
            long start_timer=System.currentTimeMillis();
              Connection.Response resp_conn=conn.method(Connection.Method.POST).execute();
            long end_timer=System.currentTimeMillis();
            long time=end_timer-start_timer;
           
            Document doc = resp_conn.parse();
             
             
            //what?!?!??!?!??
            
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
                   // System.out.println("VADO AL POST");
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
                    
                    // System.out.println("--------> ACTION:" +link.text()+" "+link.attr("abs:action")+" numero: "+counterLink);


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

                   // System.out.println("questa è l'action ---> "+link.attr("abs:action"));

                    link.attr("action", "Dispatch");  //qui cambio gli attributi
                }
            }
            
            
            
           // System.out.println(doc.head());
            // System.out.println(doc.body());
            
            
            
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
                     //System.out.println("vecchio testo: "+text_script);
                     String regeJsNoAbs="(\"|')(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|](\"|')";
                     Pattern pattern = Pattern.compile(regeJsNoAbs);
                     Matcher matcher = pattern.matcher(text_script);
                   
                      while(matcher.find())
                      {
                            String url_script=matcher.group();
                    
                            String linkName = "id"+counterLink;
                            counterLink++;
                            
                            
                           // System.out.println("Provo a sostituire "+url_script+" con "+"Dispatch?link="+linkName);
                            
                            text_script=text_script.replaceAll(url_script,"'Dispatch?link="+linkName+"'");
                           
                          //  System.out.println("NUovo testo:\n"+text_script+"\n\n");
                            
                            script.
                                    
                                    text(text_script);
                            url_script=url_script.substring(1);
                            url_script=url_script.substring(0,url_script.length()-1);
                            hashLink.put(linkName, url_script);
                            
                            
                      }
                }
            }
            
   
            //set cookies for response
            for(int i=0;i<cookies.length;i++)
            {
                //System.out.println(cookies[i].getName()+ " "+cookies[i].getValue());
                response.addCookie(cookies[i]);
            }

       
            //send response page to the client
              FileOutputStream fos = new FileOutputStream("temp.txt");
             PrintWriter pw = new PrintWriter(fos);
             pw.print(doc.html());
             
             FileReader fr = new FileReader("temp.txt");
             JSParser p = new JSParser(fr);   
             p.set_request_time(time+", POST");
			
			Symbol s;
			while ((s = p.next_token()).sym != -1999) {}
                        
                        
         /*   String mypage=p.getModFile();
             int insert=mypage.indexOf("<head");
             char c=mypage.charAt(insert+1);
             while(c!='>')
             { insert++; c=mypage.charAt(insert); }
             String first=mypage.substring(0,insert+1);
             System.out.println("FIRST" + first);
             String follow=mypage.substring(insert+1, mypage.length());
             mypage=first+myhead+follow;
           */
             
             response.getOutputStream().write(p.getModFile().getBytes());
            
    }

    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    
    
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

