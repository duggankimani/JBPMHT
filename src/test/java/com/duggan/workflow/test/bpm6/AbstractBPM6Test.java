package com.duggan.workflow.test.bpm6;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.duggan.workflow.client.util.Definitions;
import com.duggan.workflow.server.ServerConstants;
import com.duggan.workflow.server.db.DB;
import com.duggan.workflow.server.db.DBTrxProviderImpl;
import com.duggan.workflow.server.guice.UserTransactionProvider;
import com.duggan.workflow.server.helper.jbpm.JBPMHelper;
import com.duggan.workflow.server.helper.session.SessionHelper;
import com.duggan.workflow.shared.model.HTUser;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.gwtplatform.dispatch.rpc.shared.DispatchService;

//@RunWith(OnamiRunner.class)
@RunWith(MockitoJUnitRunner.class)
public class AbstractBPM6Test {

	@Rule public MockitoRule mockitoRule = new MockitoRule();

	private @Mock HttpServletRequest mockrequest;
	private @Mock HttpSession mocksession;
	private @Mock Cookie cookie;
	
	protected Injector injector;
	
	protected UserTransactionProvider trxProvider;
	
	protected DispatchService dispatchService;

	
	@Before
	public void setup() throws NotSupportedException, SystemException{
		Assert.assertNotNull(mockrequest);
		Assert.assertNotNull(mocksession);
		Mockito.when(mockrequest.getSession(false)).thenReturn(mocksession);
		Mockito.when(mockrequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8888/wiratest"));
		Mockito.when(mockrequest.getServletPath()).thenReturn("");
		Mockito.when(mockrequest.getSession(false)).thenReturn(mocksession);
		Mockito.when(mockrequest.getSession(false)).thenReturn(mocksession);
		
		Mockito.when(mocksession.getAttribute(Definitions.AUTHENTICATIONCOOKIE)).thenReturn("AUTHCOOKIE");
		
		Cookie[] cookies= new Cookie[1];
		cookies[0] = new Cookie(Definitions.AUTHENTICATIONCOOKIE, "AUTHCOOKIE");
		Mockito.when(mockrequest.getCookies()).thenReturn(cookies);
		
		Mockito.when(mocksession.getAttribute(ServerConstants.USER))
		.thenReturn(new HTUser("Administrator", "kimani@wira.io"));
		SessionHelper.setHttpRequest(mockrequest);
		
		
		injector = Guice.createInjector(new BaseModule(), new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(HttpServletRequest.class).toInstance(mockrequest);
				bind(HttpSession.class).toInstance(mocksession);
			}
		});
		
		JBPMHelper.get();
		
		trxProvider = injector.getInstance(UserTransactionProvider.class);
		trxProvider.get().begin();
		DB.getEntityManager().joinTransaction();
		dispatchService = injector.getInstance(DispatchService.class);
	}
	

	@After
	public void commit() {
		try {
			trxProvider.get().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void rollback() {
		try {
			trxProvider.get().rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@AfterClass
	public static void shutdown() {
		DBTrxProviderImpl.close();
	}

		
}
