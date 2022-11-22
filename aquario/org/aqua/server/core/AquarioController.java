package org.aqua.server.core;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.aqua.server.socket.ClientService;

public class AquarioController extends HttpServlet {
	private ClientService mc;

	@Override
	public void init(ServletConfig sc) {
		mc = new ClientService();
		mc.start();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("airtemp", "----");
		request.setAttribute("watertemp", "----");
		request.setAttribute("humidity", "----");
		if (mc.isClientConnected()) {
			request.setAttribute("connected", "подключён");
		}	
		else {
			request.setAttribute("connected", "отключён");
		}
		request.getRequestDispatcher("home.jsp").forward(request, response);
		}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("airtemp", "----");
		request.setAttribute("watertemp", "----");
		request.setAttribute("humidity", "----");
		if (request.getParameter("lighton") != null) {
			mc.lightOn();
		}
		else if (request.getParameter("lightoff") != null) {
			mc.lightOff();
		}
		if (request.getParameter("pompon") != null) {
			mc.pompOn();
		}
		else if (request.getParameter("pompoff") != null) {
			mc.pompOff();
		}
		else if (request.getParameter("feed") != null) {
			mc.feed();
		}
		else if (request.getParameter("airtemp") != null) {
			request.setAttribute("airtemp", mc.atemp());
		}
		else if (request.getParameter("watertemp") != null) {
			request.setAttribute("watertemp", mc.wtemp());
		}
		else if (request.getParameter("humidity") != null) {
			request.setAttribute("humidity", mc.humid());
		}
		if (mc.isClientConnected()) {
			request.setAttribute("connected", "подключён");
		}
		else {
			request.setAttribute("connected", "отключён");
		}
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}

	@Override
	public void destroy() {
		mc.quit();
	}
}
