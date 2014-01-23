package besouro.listeners;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
//import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.cdt.core.model.ElementChangedEvent;

//import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.cdt.core.model.IElementChangedListener;
//import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.cdt.core.model.ICElement;

//import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.cdt.core.model.ICElementDelta;

import besouro.model.RefactoringAction;
import besouro.stream.ActionOutputStream;


/**
 * Listens to the java element change events to get incremental work on java
 * objects and collect refactoring information for test-driven development
 * analysis purpose. It's declared as package private so that it can only be
 * instantiated by Eclise sensor.
 * 
 * @author Hongbing Kou
 */
public class JavaStructureChangeListener implements IElementChangedListener {

	public static final String JAVA = "java";
	public static final String CLASS = "Class";

	protected static final String PROP_CURRENT_SIZE = "Current-Size";
	protected static final String PROP_CLASS_NAME = "Class-Name";
	protected static final String PROP_CURRENT_METHODS = "Current-Methods";
	protected static final String PROP_CURRENT_STATEMENTS = "Current-Statements";
	protected static final String PROP_CURRENT_TEST_METHODS = "Current-Test-Methods";
	protected static final String PROP_CURRENT_TEST_ASSERTIONS = "Current-Test-Assertions";

	private ActionOutputStream stream;

	public JavaStructureChangeListener(ActionOutputStream stream) {
		this.stream = stream;
	}

	public void elementChanged(ElementChangedEvent event) {
		// IJavaElementDelta jed = event.getDelta().getAffectedChildren()[0];
		ICElementDelta[] childrenChanges = event.getDelta().getAffectedChildren();

		if (childrenChanges != null && childrenChanges.length > 0) {
			javaObjectChange(childrenChanges[0]);
		}
	}

	private void javaObjectChange(ICElementDelta jed) {
		List<ICElementDelta> additions = new ArrayList<ICElementDelta>();
		List<ICElementDelta> deletions = new ArrayList<ICElementDelta>();

		// Traverse the delta change tree for refactoring activity
		traverse(jed, additions, deletions);

		// Gets the location of java file.
		IResource javaFile = jed.getElement().getResource();

		// No java structure change
		if (additions.isEmpty() && deletions.isEmpty()) {
			return;

			// Addition, deletion, renaming activity.
		} else if (additions.size() == 1 || deletions.size() == 1) {

			if (deletions.isEmpty()) {
				processUnary(javaFile, "ADD", (ICElementDelta) additions.get(0));

			} else if (additions.isEmpty()) {
				processUnary(javaFile, "REMOVE", (ICElementDelta) deletions.get(0));

			} else if (deletions.size() == 1) {

				ICElementDelta fromDelta = (ICElementDelta) deletions.get(0);
				ICElementDelta toDelta = (ICElementDelta) additions.get(0);

				if (fromDelta.getElement().getParent().equals(toDelta.getElement().getParent())) {
					processRenameRefactor(javaFile, fromDelta, toDelta);

				} else {
					processMoveRefactor(fromDelta, toDelta);
				}

			}
		}
		// Massive addition by copying
		else if (additions.size() > 1) {

			for (Iterator i = additions.iterator(); i.hasNext();) {
				processUnary(javaFile, "ADD",(ICElementDelta) i.next());
			}
		}
		// Massive block deletion
		else if (deletions.size() > 1) {
			for (Iterator i = deletions.iterator(); i.hasNext();) {
				processUnary(javaFile, "REMOVE",(ICElementDelta) i.next());
			}
		}
	}


	private void processUnary(IResource javaFile, String op, ICElementDelta delta) {
		
		ICElement element = delta.getElement();

		// Stop if there is no associated element.
		if (javaFile == null || element == null || element.getResource() == null) {
			return;
		}

		String type = retrieveType(element);
		// If type is not field, method, import and class do nothing.
		if (type == null) {
			return;
		}

		IPath classFileName = javaFile.getLocation();
		if ("CLASS".equals(type)) {
			classFileName = element.getResource().getLocation();
		}

		// Only deal with java file.
		if (!JAVA.equals(classFileName.getFileExtension())) {

			return;
		}

		String name = buildElementName(element.toString());
		if (name != null && !"".equals(name)) {

			RefactoringAction action = new RefactoringAction(new Date(), element.getResource().getName());
			action.setOperator(op);
			action.setSubjectType(type);
			action.setSubjectName(name);

			this.stream.addAction(action);

		}
	}

	private void processRenameRefactor(IResource javaFile, ICElementDelta fromDelta, ICElementDelta toDelta) {

		String type = retrieveType(toDelta.getElement());

		IPath classFileName = javaFile.getLocation();
		if ("CLASS".equals(type)) {
			classFileName = fromDelta.getElement().getResource().getLocation();

		} else if ("PACKAGE".equals(type)) {
			classFileName = fromDelta.getElement().getResource().getLocation();
		}

		// Only deal with java file.
		if (!JAVA.equals(classFileName.getFileExtension())) {
			return;
		}

		String fromName = buildElementName(fromDelta.getElement().toString());
		String toName = buildElementName(toDelta.getElement().toString());

		if (fromName != null && !"".equals(fromName) && toName != null && !"".equals(toName)) {

			RefactoringAction action = new RefactoringAction(new Date(), javaFile.getName());
			action.setOperator("RENAME");
			action.setSubjectName(fromName + "=>" + toName);

			action.setSubjectType(type);

			this.stream.addAction(action);

		}
	}

	private void processMoveRefactor(ICElementDelta fromDelta, ICElementDelta toDelta) {

		IResource javaFile = fromDelta.getElement().getResource();
		ICElement from = fromDelta.getElement();
		ICElement to = toDelta.getElement().getParent();

		// Only deal with java file.
		if (!JAVA.equals(javaFile.getFileExtension())) {
			return;
		}

		String fromName = buildElementName(from.toString());
		String toName = buildElementName(to.toString());

		if (fromName != null && !"".equals(fromName) && toName != null && !"".equals(toName)) {

			RefactoringAction action = new RefactoringAction(new Date(), javaFile.getName());
			action.setOperator("MOVE");
			action.setSubjectName(fromName + "=>" + toName);

			action.setSubjectType(retrieveType(toDelta.getElement()));

			this.stream.addAction(action);

		}
	}

	private String retrieveType(ICElement element) {
		int eType = element.getElementType();

		switch (eType) {
		case ICElement.C_FIELD:
			return "FIELD";
		case ICElement.C_METHOD:
			return "METHOD";
//		case ICElement.IMPORT_DECLARATION:
//			return "IMPORT";
//		case ICElement.IMPORT_CONTAINER:
//			return "IMPORT";
//		case ICElement.COMPILATION_UNIT:
//			return "CLASS";
		case ICElement.C_PROJECT:
			return "CLASS";
//		case IJavaElement.PACKAGE_FRAGMENT:
//			return "PACKAGE";
		default:
			return null;
		}
	}

	private String buildElementName(String name) {

		int index = name.indexOf('[');
		if (index >=0)
			name = name.substring(0, index);
		
		// Trim off the meaningless "(not open)" string
		int pos = name.indexOf("(not open)");
		if (pos > 0) {
			name = name.substring(0, pos);
		}

		// take off the '#' if it exists
		name = name.replace('#', '/');

		return name.trim();
	}


	private void traverse(ICElementDelta delta, List<ICElementDelta> additions, List<ICElementDelta> deletions) {

		// Saves the addition and deletion.
		if (delta.getKind() == ICElementDelta.ADDED) {
			additions.add(delta);

		} else if (delta.getKind() == ICElementDelta.REMOVED) {
			deletions.add(delta);
		}

		// Recursively look for changes on children elements.
		ICElementDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; i++) {
			traverse(children[i], additions, deletions);
		}
	}

	public void elementChanged(org.eclipse.jdt.core.ElementChangedEvent event) {
		// TODO Auto-generated method stub
		
	}
}