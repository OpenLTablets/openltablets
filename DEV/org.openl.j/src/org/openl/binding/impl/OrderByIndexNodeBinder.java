package org.openl.binding.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import org.openl.binding.IBindingContext;
import org.openl.binding.IBoundNode;
import org.openl.binding.ILocalVar;
import org.openl.exception.OpenLRuntimeException;
import org.openl.syntax.ISyntaxNode;
import org.openl.syntax.impl.ISyntaxConstants;
import org.openl.types.IAggregateInfo;
import org.openl.types.IOpenClass;
import org.openl.types.NullOpenClass;
import org.openl.vm.IRuntimeEnv;

/**
 * Binds conditional index for arrays like: - arrayOfDrivers[@ age < 20]; -
 * arrayOfDrivers[select all having gender == "Male"]
 * 
 * @author PUdalau
 */
public class OrderByIndexNodeBinder extends BaseAggregateIndexNodeBinder {

	private static final String TEMPORARY_VAR_NAME = "OrderByIndex";

	private static class OrderList extends ArrayList<Object> {
		private static final long serialVersionUID = 1L;
	}

	private static class OrderByIndexNode extends ABoundNode {

		private ILocalVar tempVar;
		private boolean isDecreasing;

		public OrderByIndexNode(ISyntaxNode syntaxNode, IBoundNode[] children,
				ILocalVar tempVar, boolean isDecreasing) {
			super(syntaxNode, children);
			this.tempVar = tempVar;
			this.isDecreasing = isDecreasing;
		}

		public Object evaluateRuntime(IRuntimeEnv env)
				throws OpenLRuntimeException {
			IBoundNode containerNode = getContainer();
			IBoundNode orderBy = getChildren()[1];
			IAggregateInfo aggregateInfo = containerNode.getType()
					.getAggregateInfo();
			Object container = containerNode.evaluate(env);

			Iterator<Object> elementsIterator = aggregateInfo
					.getIterator(container);

			TreeMap<Comparable<?>, Object> map = new TreeMap<Comparable<?>, Object>();

			int size = 0;
			while (elementsIterator.hasNext()) {
				Object element = elementsIterator.next();
				tempVar.set(null, element, env);
				Comparable<?> key = (Comparable<?>) orderBy.evaluate(env);
				Object prev = map.put(key, element);
				if (prev != null) {
					OrderList list = null;
					if (prev.getClass() != OrderList.class) {
						list = new OrderList();
						list.add(prev);
					} else
						list = (OrderList) prev;

					list.add(element);
					map.put(key, list);
				}
				++size;
			}

			Object result = aggregateInfo.makeIndexedAggregate(
					aggregateInfo.getComponentType(getType()),
					new int[] { size });

			Iterator<Object> mapIterator = map.values().iterator();
			int idx = 0;
			while (mapIterator.hasNext()) {
				Object element = mapIterator.next();
				if (element.getClass() != OrderList.class)
					Array.set(result, nextIdx(idx++, size), element);
				else {
					OrderList list = (OrderList) element;
					for (int i = 0; i < list.size(); i++) {
						Array.set(result, nextIdx(idx++, size), list.get(i));
					}
				}
			}
			return result;
		}

		private int nextIdx(int idx, int size) {
			return isDecreasing ? size - 1 - idx : idx;
		}

		private IBoundNode getContainer() {
			return getChildren()[0];
		}

		public IOpenClass getType() {
			if (getContainer().getType().isArray())
				return getContainer().getType();

			IOpenClass varType = tempVar.getType();
			return varType.getAggregateInfo().getIndexedAggregateType(varType,
					1);
		}
	}


	static public IBoundNode checkOrderExpressionBoundNode(
			IBoundNode orderExpressionNode, IBindingContext bindingContext) {

		if (orderExpressionNode != null
				&& !Comparable.class.isAssignableFrom(orderExpressionNode
						.getType().getInstanceClass())) {

			if (orderExpressionNode.getType() != NullOpenClass.the) {
				BindHelper.processError(
						"Order By expression must be Comparable",
						orderExpressionNode.getSyntaxNode(), bindingContext);
			}
			return new ErrorBoundNode(orderExpressionNode.getSyntaxNode());
		} else {
			return orderExpressionNode;
		}

	}

	@Override
	public String getDefaultTempVarName(IBindingContext bindingContext) {
		return BindHelper.getTemporaryVarName(bindingContext,
				ISyntaxConstants.THIS_NAMESPACE, TEMPORARY_VAR_NAME);
	}

	@Override
	protected IBoundNode createBoundNode(ISyntaxNode node,
			IBoundNode targetNode, IBoundNode expressionNode, ILocalVar localVar) {
		boolean isDecreasing = node.getType().contains("decreasing");
		return new OrderByIndexNode(node, new IBoundNode[] { targetNode,
				expressionNode }, localVar, isDecreasing);
	}

	@Override
	protected IBoundNode validateExpressionNode(IBoundNode expressionNode,
			IBindingContext bindingContext) {
		return checkOrderExpressionBoundNode(expressionNode, bindingContext);
	}
}
