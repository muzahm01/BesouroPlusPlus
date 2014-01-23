package besouro.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.eclipse.cdt.internal.core.model.CProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.cdt.core.model.CModelException;
//import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.cdt.core.model.ICElement;

//import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.cdt.core.model.ICProject;

//import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.junit.TestRunListener;

//import org.eclipse.jdt.junit.model.ITestCaseElement;
import org.eclipse.cdt.testsrunner.model.ITestCase;
//import org.eclipse.jdt.junit.model.ITestElement;
//import org.eclipse.jdt.junit.model.ITestElement.Result;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
//import org.eclipse.jdt.junit.model.ITestElementContainer;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
//import org.eclipse.jdt.junit.model.ITestRunSession;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
//import org.eclipse.jdt.junit.model.ITestSuiteElement;







import besouro.model.UnitTestAction;
import besouro.model.UnitTestCaseAction;
import besouro.model.UnitTestSessionAction;
import besouro.stream.ActionOutputStream;


public class JUnitListener extends ITestingSessionListener {
//	public class JUnitListener extends TestRunListener {

	
	private ActionOutputStream stream;

	public JUnitListener(ActionOutputStream stream) {
		this.stream = stream;
	}
//	ICProject project= getCProject();
//	@Override
	public void sessionFinished(ITestingSession session, ICProject project) {
		// New code by muzamil to get C Project
//		IProject cp=CProject.getProject();
//		cp.getProject();
		boolean isSuccessfull = true;
		for (UnitTestAction action: getTestFileActions(session, session.getTestsRunnerProviderInfo())) {
			stream.addAction(action);
			isSuccessfull &= action.isSuccessful();
		}
		
//		IResource res = findTestResource(session.getLaunchedProject(), session.getTestRunName());
		IResource res = findTestResource(session.getLaunchedProject(), session.getName());

//		String name = res!=null?res.getName():session.getTestRunName();
		String name = res!=null?res.getName():session.getName();
		
		// registers the session action. It brakes the episode, but doesnt count on the classification
		UnitTestSessionAction action = new UnitTestSessionAction(new Date(), name);
		action.setSuccessValue(isSuccessfull);
		stream.addAction(action);
		
	}

	private Collection<UnitTestCaseAction> getTestFileActions(ITestItem session, ICProject project) {
		
		List<UnitTestCaseAction> list = new ArrayList<UnitTestCaseAction>();
		
//		if (session instanceof ITestSuiteElement) {
		if (session instanceof ITestSuite) {
//			ITestSuiteElement testCase = (ITestSuiteElement) session;
			ITestSuite testCase = (ITestSuite) session;
//			IResource res = findTestResource(project, testCase.getSuiteTypeName());
			IResource res = findTestResource(project, testCase.getName());
			
			UnitTestCaseAction action = new UnitTestCaseAction(new Date(), res.getName());
//			action.setSuccessValue(testCase.getTestResult(true).equals(Result.OK));
			action.setSuccessValue(testCase.getStatus().equals(Status.Passed));
			list.add(action);
			
//		} else if (session instanceof ITestCaseElement) {
		} else if (session instanceof ITestCase) {
				
//			ITestCaseElement testCase = (ITestCaseElement) session;
			ITestCase testCase = (ITestCase) session;
			
//			IResource res = findTestResource(project, testCase.getTestClassName());
			IResource res = findTestResource(project, testCase.getName());
				
			// will reach this case only when user executes a single test method
			
			UnitTestCaseAction action = new UnitTestCaseAction(new Date(),res.getName());
//			action.setSuccessValue(testCase.getTestResult(true).equals(Result.OK));
			action.setSuccessValue(testCase.getStatus().equals(Status.Passed));
			
			list.add(action);
						
		} else if (session instanceof ITestSuite) {
			ITestSuite container = (ITestSuite) session; 
			for(ITestItem child: container.getChildren()){
				list.addAll(getTestFileActions(child, project));
			}
		}
		
		
		return list;
		
	}

	private IResource findTestResource(ICProject project, String className) {
		IPath path = new Path(className.replaceAll("\\.", "/") + ".java");
		try {
			
			ICElement element = project.findElement(path);
			
			if (element != null)
				return element.getResource();
			else 
				return null;
			
		} catch (CModelException e) {
			throw new RuntimeException(e);
		}
	}
//	public ICProject getCProject() {
//		ICElement current = this;
//		do {
//			if (current instanceof ICProject)
//				return (ICProject) current;
//		} while ((current = current.getParent()) != null);
//		return null;
//	}
//	protected ICProject getCProject() {
//		ITextEditor editor= getEditor();
//		if (editor == null)
//			return null;
//
//		ICElement element= null;
//		IEditorInput input= editor.getEditorInput();
//		IDocumentProvider provider= editor.getDocumentProvider();
//		if (provider instanceof CDocumentProvider) {
//			CDocumentProvider cudp= (CDocumentProvider) provider;
//			element= cudp.getWorkingCopy(input);
//		}
//
//		if (element == null)
//			return null;
//
//		return element.getCProject();
//	}


//	 private void print(ITestElement session) {
//		
//		
//		 if (session instanceof ITestSuiteElement) {
//			 
//			 ITestSuiteElement suite = (ITestSuiteElement) session;
//			 
//		 } else if (session instanceof ITestElementContainer) {
//			 
//			 ITestElementContainer suite = (ITestElementContainer) session;
//			
//			 for (ITestElement test : suite.getChildren()) {
//				 print(test);
//			 }
//			
//		 }
//		 
//	 }

}
