package besouro.measure;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
//import org.eclipse.jdt.core.ICompilationUnit;
//import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
//import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
//import org.eclipse.jdt.core.dom.Annotation;
//import org.eclipse.jdt.core.dom.Block;
//import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import besouro.model.JavaFileAction;

/**
 * Implements a meter to measure several aspects such as number of methods,
 * statements, test methods, assertion statements if applicable. Number of test
 * methods, assertion statements is reported to assist Test-Driven Development
 * study.
 * 
 * @author Hongbing Kou
 */
//public class JavaStatementMeter extends ASTVisitor {

public class JavaStatementMeter extends ASTVisitor {

	private String name;
	private String packageName;

	private int numOfMethods = 0;
	private int numOfStatements = 0;

	private int numOfTestMethods = 0;
	private int numOfTestAssertions = 0;
//
//
//	public void reset() {
//		name = null;
//		numOfMethods = 0;
//		numOfStatements = 0;
//		numOfTestMethods = 0;
//		numOfTestAssertions = 0;
//
//	}
//
//	public boolean visit(PackageDeclaration node) {
//		this.packageName = node.getName().getFullyQualifiedName();
//		return true;
//	}
//	
//	public boolean visit(TypeDeclaration td) {
//		if (td.getName() != null) {
//			this.name = td.getName().getIdentifier();
//		}
//
//		return true;
//	}
	
	//Removing following function from list as we have to check GTest now

//	private boolean isJUnit4Test(MethodDeclaration md) {
//		List modifiers = md.modifiers();
//		for (Iterator i = modifiers.iterator(); i.hasNext();) {
//			IExtendedModifier modifer = (IExtendedModifier) i.next();
//			if (modifer.isAnnotation()) {
//				Annotation annotation = (Annotation) modifer;
//				if ("Test".equals(annotation.getTypeName()
//						.getFullyQualifiedName())) {
//					return true;
//				}
//			}
//		}
//
//		return false;
//	}

	public boolean visit(CPPASTFunctionDefinition md) {
//		public boolean visit(MethodDeclaration md) {
			
		if (md.getDeclarator().getName() != null) {
			this.numOfMethods++;

			if (md.getDeclarator().getName().toString().startsWith("TEST_F")) {
				this.numOfTestMethods++;
			} 
//				else if (isJUnit4Test(md)) {
//				this.numOfTestMethods++;
//			}

			// Check test method body to look for assertion statement.
//			Block methodBody = md.getBody();
			CPPASTCompoundStatement methodBody = (CPPASTCompoundStatement) md.getBody();
//			if (methodBody != null && methodBody.statements() != null) {
			if (methodBody != null && methodBody.getStatements() != null) {
					
//				List stmts = methodBody.statements();
				IASTStatement[] stmts = methodBody.getStatements();
//				this.numOfStatements += stmts.size();
				this.numOfStatements += stmts.length;
				// Looks through all statements in this method body.
//				for (Iterator i = stmts.iterator(); i.hasNext();) {
//					Statement stmt = (Statement) i.next(); // NOPMD
//					// MethodInvocation is one kind of expression statement.
//					if (stmt instanceof ExpressionStatement) {
//						ExpressionStatement estmt = (ExpressionStatement) stmt;
//						checkAssertions(estmt);
//					}
					for (int i =0; i<stmts.length; i++) {
						Statement stmt = (Statement) stmts[i]; // NOPMD
						// MethodInvocation is one kind of expression statement.
						
//						if (stmt instanceof ExpressionStatement) {
//							ExpressionStatement estmt = (ExpressionStatement) stmt;
//							checkAssertions(estmt);
//						}
						if (stmt instanceof IASTExpressionStatement) {
							IASTExpressionStatement estmt = (IASTExpressionStatement) stmt;
							checkAssertions(estmt);
						}

				}
			}
		}

		// No need to visit child nodes anymore.
		return false;
	}

	private void checkAssertions(IASTExpressionStatement estmt) {
		if (estmt.getExpression() instanceof MethodInvocation) {
			MethodInvocation mi = (MethodInvocation) estmt.getExpression();
			// Increment number of test assertions.
			if (mi.getName() != null
					&& mi.getName().getIdentifier().startsWith("assert")) {
				this.numOfTestAssertions++;
			}
		}
	}

//	public int getNumOfMethods() {
//		return this.numOfMethods;
////	}
//
//	public int getNumOfStatements() {
//		return this.numOfStatements;
//	}
//
//	public int getNumOfTestMethods() {
//		return this.numOfTestMethods;
//	}
//
//	public int getNumOfTestAssertions() {
//		return this.numOfTestAssertions;
//	}
//
//	public boolean hasTest() {
//		return this.numOfTestMethods > 0 || this.numOfTestAssertions > 0;
//	}
//
//	public boolean isTest() {
//		// minimizes the problem of the case of the first class' test method creation
//		// (looking for 'test' in the class or package name
//		boolean hasTestInPackageName = this.packageName!=null && this.packageName.toLowerCase().indexOf("test") >= 0;
//		boolean hasTestInClassName = this.name.toLowerCase().indexOf("test") >= 0;
//		return hasTest() || hasTestInClassName || hasTestInPackageName;
//	}
//	
//	public String toString() {
//		StringBuffer buf = new StringBuffer(200);
//		buf.append("*****  ").append(this.name)
//				.append("   *****\nMethods     : ").append(this.numOfMethods)
//				.append("\nStatements  : ").append(this.numOfStatements);
//
//		// Appends test info if there is any.
//		if (this.hasTest()) {
//			buf.append("\nTests       : ").append(this.numOfTestMethods);
//			buf.append("\nAssertions  : ").append(this.numOfTestAssertions);
//		}
//
//		return buf.toString();
//	}

	public JavaStatementMeter measureJavaFile(IFile file) {
		// Compute number of tests and assertions to this file.

//		ICompilationUnit cu = (ICompilationUnit) JavaCore.create(file);
//		ASTParser parser = ASTParser.newParser(AST.JLS3);
//		parser.setSource(cu);
//		parser.setResolveBindings(true);
//
//		ASTNode root = parser.createAST(null);
		ITranslationUnit tu = (ITranslationUnit)CoreModel.getDefault().create(file);

		IASTTranslationUnit ast = null;
		try {
			ast = tu.getAST();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JavaStatementMeter meter = new JavaStatementMeter();
//		root.accept(meter);
		ast.accept(meter);
		return meter;
	}
	

}
