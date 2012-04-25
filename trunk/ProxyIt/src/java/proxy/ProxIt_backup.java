package proxy;

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

public class ProxIt_backup extends HttpServlet {
	
	private ServletContext servletContext;
	private Logger log;
	
    public void init(ServletConfig servletConfig) throws ServletException {
    	servletContext = servletConfig.getServletContext();
        log = Logger.getLogger(ProxIt_backup.class.getName());
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
    
		BufferedInputStream webToProxyBuf = null;
		BufferedOutputStream proxyToClientBuf = null;
		HttpURLConnection con;
		
		try{
			int statusCode;
			int oneByte;
			String methodName;
			String headerText;
			String urlString = request.getRequestURL().toString();
			String queryString = request.getQueryString();
			
			//urlString += queryString==null?"":"?"+queryString;
			URL url = new URL(request.getParameter("proxit"));
			
			log.info("Fetching >"+request.getParameter("proxit").toString());
			
			con =(HttpURLConnection) url.openConnection();
			
			methodName = request.getMethod();
			con.setRequestMethod(methodName);
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setFollowRedirects(false);
			con.setUseCaches(true);

			for( Enumeration e = request.getHeaderNames() ; e.hasMoreElements();){
				String headerName = e.nextElement().toString();
				con.setRequestProperty(headerName,	request.getHeader(headerName));
			}

			con.connect();
			
			if(methodName.equals("POST")){
				BufferedInputStream clientToProxyBuf = new BufferedInputStream(request.getInputStream());
				BufferedOutputStream proxyToWebBuf 	= new BufferedOutputStream(con.getOutputStream());
				
				while ((oneByte = clientToProxyBuf.read()) != -1) 
					proxyToWebBuf.write(oneByte);
				
				proxyToWebBuf.flush();
				proxyToWebBuf.close();
				clientToProxyBuf.close();
			}
			
			statusCode = con.getResponseCode();
			response.setStatus(statusCode);
			
			for( Iterator i = con.getHeaderFields().entrySet().iterator() ; i.hasNext() ;){
				Map.Entry mapEntry = (Map.Entry)i.next();
				if(mapEntry.getKey()!=null)
					response.setHeader(mapEntry.getKey().toString(), ((List)mapEntry.getValue()).get(0).toString());
			}
            
            /*            
            InputStream web2proxy = con.getInputStream();
            DataInputStream dis = new DataInputStream(web2proxy);
            
            ServletOutputStream s = response.getOutputStream();
            s.print("ciao");
            s.flush();
            s.close();
            */
                        
            webToProxyBuf = new BufferedInputStream(con.getInputStream());
            ServletOutputStream SerOutStream = response.getOutputStream();
            proxyToClientBuf = new BufferedOutputStream(SerOutStream);
            
            byte[] buffer = new byte[1024];
        
            
            //Construct the BufferedInputStream object
            
            
            int bytesRead = 0;
            
            //Keep reading from the file while there is any content
            //when the end of the stream has been reached, -1 is returned
            while ((bytesRead = webToProxyBuf.read(buffer)) != -1) {
                
                //Process the chunk of bytes read
                //in this case we just construct a String and print it out
                String chunk = new String(buffer, 0, bytesRead);
                chunk+="<h1>siii</h1>";
             
                proxyToClientBuf.write(chunk.getBytes(), 0, bytesRead);
            }
                        
			proxyToClientBuf.flush();
			proxyToClientBuf.close();

			webToProxyBuf.close();
			con.disconnect();
			
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		finally{
		}
                
                
	}
}