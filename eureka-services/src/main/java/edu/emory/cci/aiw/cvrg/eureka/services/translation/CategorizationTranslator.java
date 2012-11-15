/*
 * #%L
 * Eureka Services
 * %%
 * Copyright (C) 2012 Emory University
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package edu.emory.cci.aiw.cvrg.eureka.services.translation;

import java.util.ArrayList;
import java.util.List;

import edu.emory.cci.aiw.cvrg.eureka.common.comm.CategoricalElement;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.DataElement;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.PropositionWrapper;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.Categorization;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.Proposition;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.PropositionTypeVisitor;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.Categorization.CategorizationType;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.SystemProposition;
import edu.emory.cci.aiw.cvrg.eureka.services.dao.PropositionDao;

/**
 * Translates categorical data elements (UI element) into categorization
 * propositions.
 */
final class CategorizationTranslator implements
        PropositionTranslator<CategoricalElement, Categorization> {

	private final PropositionDao dao;

	public CategorizationTranslator(PropositionDao dao) {
		this.dao = dao;
	}

	@Override
	public Categorization translate(CategoricalElement element) {
		Categorization result = new Categorization();
		PropositionTranslatorUtil.populateCommonFields(result, element);

		PropositionTypeVisitor visitor = new PropositionTypeVisitor();
		CategorizationType type = CategorizationType.UNKNOWN;
		List<Proposition> inverseIsA = new ArrayList<Proposition>();
		for (DataElement de : element.getChildren()) {
			Proposition child = dao.retrieve(de.getId());
			inverseIsA.add(child);
			child.accept(visitor);
			CategorizationType childType = checkType(child, visitor.getType());
			if (type == CategorizationType.UNKNOWN) {
				type = childType;
			} else if (childType != type) {
				type = CategorizationType.MIXED;
			}

		}
		result.setInverseIsA(inverseIsA);
		result.setCategorizationType(type);

		return result;
	}

	private CategorizationType checkType(Proposition child,
	        PropositionWrapper.Type type) {
		switch (type) {
			case CATEGORIZATION:
				return ((Categorization) child).getCategorizationType();
			case SEQUENCE:
				// fall through
			case FREQUENCY:
				// fall through
			case VALUE_THRESHOLD:
				return CategorizationType.ABSTRACTION;
			case SYSTEM:
				switch (((SystemProposition) child).getSystemType()) {
					case CONSTANT:
						return CategorizationType.CONSTANT;
					case EVENT:
						return CategorizationType.EVENT;
					case PRIMITIVE_PARAMETER:
						return CategorizationType.PRIMITIVE_PARAMETER;
					case HIGH_LEVEL_ABSTRACTION:
						// fall through
					case LOW_LEVEL_ABSTRACTION:
						// fall through
					case SLICE_ABSTRACTION:
						return CategorizationType.ABSTRACTION;
				}
		}
		return CategorizationType.UNKNOWN;
	}
}
