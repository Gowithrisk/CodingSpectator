/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.CorrectiveRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperty;


/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class InferredRefactoring {

	private final Map<String, RefactoringProperty> properties= new HashMap<String, RefactoringProperty>();


	protected abstract Set<String> getAcceptableProperties();

	public abstract RefactoringKind getKind();

	protected abstract InferredRefactoring createFreshInstance();

	/**
	 * Should be called only for complete refactorings.
	 * 
	 * @return
	 */
	public abstract Map<String, String> getArguments();

	protected static void addProperty(InferredRefactoring inferredRefactoring, RefactoringProperty refactoringProperty) {
		inferredRefactoring.properties.put(refactoringProperty.getClassName(), refactoringProperty);
	}

	protected RefactoringProperty getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	public boolean isComplete() {
		for (String acceptableProperty : getAcceptableProperties()) {
			if (properties.get(acceptableProperty) == null) {
				return false;
			}
		}
		return true;
	}

	public boolean canBePart(RefactoringProperty refactoringProperty) {
		if (refactoringProperty instanceof CorrectiveRefactoringProperty) {
			return canBeCorrectivePart((CorrectiveRefactoringProperty)refactoringProperty);
		}
		if (!getAcceptableProperties().contains(refactoringProperty.getClassName()) ||
				properties.get(refactoringProperty.getClassName()) != null) {
			return false;
		}
		for (RefactoringProperty property : properties.values()) {
			if (!property.doesMatch(refactoringProperty)) {
				return false;
			}
		}
		return true;
	}

	private boolean canBeCorrectivePart(CorrectiveRefactoringProperty correctiveProperty) {
		for (RefactoringProperty property : properties.values()) {
			//It is important that 'doesMatch' is called on the corrective property!
			if (!correctiveProperty.doesMatch(property)) {
				return false;
			}
		}
		for (RefactoringProperty property : properties.values()) {
			if (correctiveProperty.doesOverlap(property)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Should not change this refactoring, but rather should return a new one, with this refactoring
	 * property added to it.
	 * 
	 * @param refactoringProperty
	 */
	public InferredRefactoring addProperty(RefactoringProperty refactoringProperty) {
		if (!canBePart(refactoringProperty)) {
			throw new RuntimeException("Can not add property: " + refactoringProperty);
		}
		if (refactoringProperty instanceof CorrectiveRefactoringProperty) {
			return addCorrectiveProperty((CorrectiveRefactoringProperty)refactoringProperty);
		}
		InferredRefactoring resultRefactoring= createCopy();
		addProperty(resultRefactoring, refactoringProperty);
		return resultRefactoring;
	}

	private InferredRefactoring addCorrectiveProperty(CorrectiveRefactoringProperty correctiveProperty) {
		InferredRefactoring resultRefactoring= createFreshInstance();
		for (Entry<String, RefactoringProperty> propertyEntry : properties.entrySet()) {
			RefactoringProperty resultProperty= propertyEntry.getValue().createLinkedCopy();
			correctiveProperty.correct(resultProperty);
			resultRefactoring.properties.put(propertyEntry.getKey(), resultProperty);
		}
		return resultRefactoring;
	}

	private InferredRefactoring createCopy() {
		InferredRefactoring copyRefactoring= createFreshInstance();
		copyRefactoring.properties.putAll(properties);
		return copyRefactoring;
	}

	public void disableProperties() {
		for (RefactoringProperty property : properties.values()) {
			property.disable();
		}
	}

	public boolean checkDisabled() {
		Iterator<RefactoringProperty> propertiesIterator= properties.values().iterator();
		while (propertiesIterator.hasNext()) {
			if (!propertiesIterator.next().isActive()) {
				propertiesIterator.remove();
			}
		}
		return properties.size() == 0;
	}

}
