package org.openl.rules.dependency.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.openl.binding.BindingDependencies;
import org.openl.types.IOpenMethod;
import org.openl.types.impl.ExecutableMethod;

/**
 * Dependency rules graph, implemented using {@link org.jgrapht.graph.DefaultDirectedGraph}.
 * Vertexes are represented as {@link ExecutableMethod}, 
 * edges {@link org.jgrapht.graph.DefaultEdge}.
 * 
 * @author DLiauchuk
 *
 */
public class DependencyRulesGraph implements DirectedGraph<ExecutableMethod, DirectedEdge<ExecutableMethod>> {

    private DirectedGraph<ExecutableMethod, DirectedEdge<ExecutableMethod>> graph;

    public DependencyRulesGraph(List<ExecutableMethod> rulesMethods) {
        createGraph();
        fill(rulesMethods);
    }

    public DependencyRulesGraph() {
        createGraph();
    }

    private void createGraph() {
        EdgeFactory<ExecutableMethod, DirectedEdge<ExecutableMethod>> edgeFactory = 
            new DirectedEdgeFactory<ExecutableMethod>(DirectedEdge.class);
        graph = new DefaultDirectedGraph<ExecutableMethod, DirectedEdge<ExecutableMethod>>(edgeFactory);
    }

    private void fill(List<ExecutableMethod> rulesMethods) {
        if (rulesMethods != null && rulesMethods.size() > 0) {
            for (ExecutableMethod method : rulesMethods) {
                graph.addVertex(method);
                BindingDependencies dependencies = method.getDependencies();
                if (dependencies != null) {
                    Set<ExecutableMethod> dependentMethods = dependencies.getRulesMethods();
                    if (dependentMethods != null) {
                        for (ExecutableMethod dependentMethod : dependentMethods) {
                            graph.addVertex(dependentMethod);
                            graph.addEdge(method, dependentMethod);
                        }
                    }
                }
            }
        }
    }

    public DirectedEdge<ExecutableMethod> addEdge(
            ExecutableMethod sourceVertex, ExecutableMethod targetVertex) {        
        return graph.addEdge(sourceVertex, targetVertex);
    }

    public boolean addEdge(ExecutableMethod sourceVertex, ExecutableMethod targetVertex,
            DirectedEdge<ExecutableMethod> e) {        
        return graph.addEdge(sourceVertex, targetVertex, e);
    }

    public boolean addVertex(ExecutableMethod v) {        
        return graph.addVertex(v);
    }

    public boolean containsEdge(DirectedEdge<ExecutableMethod> e) {        
        return graph.containsEdge(e);
    }

    public boolean containsEdge(ExecutableMethod sourceVertex, ExecutableMethod targetVertex) {        
        return graph.containsEdge(sourceVertex, targetVertex);
    }

    public boolean containsVertex(ExecutableMethod v) {
        return graph.containsVertex(v);
    }

    public Set<DirectedEdge<ExecutableMethod>> edgeSet() {        
        return graph.edgeSet();
    }

    public Set<DirectedEdge<ExecutableMethod>> edgesOf(ExecutableMethod vertex) {        
        return graph.edgesOf(vertex);
    }

    public Set<DirectedEdge<ExecutableMethod>> getAllEdges(ExecutableMethod sourceVertex,
            ExecutableMethod targetVertex) {        
        return graph.getAllEdges(sourceVertex, targetVertex);
    }

    public DirectedEdge<ExecutableMethod> getEdge(ExecutableMethod sourceVertex,
            ExecutableMethod targetVertex) {
        return graph.getEdge(sourceVertex, targetVertex);
    }

    public EdgeFactory<ExecutableMethod, DirectedEdge<ExecutableMethod>> getEdgeFactory() {        
        return graph.getEdgeFactory();
    }

    public ExecutableMethod getEdgeSource(DirectedEdge<ExecutableMethod> e) {        
        return graph.getEdgeSource(e);
    }

    public ExecutableMethod getEdgeTarget(DirectedEdge<ExecutableMethod> e) {        
        return graph.getEdgeTarget(e);
    }

    public double getEdgeWeight(DirectedEdge<ExecutableMethod> e) {        
        return graph.getEdgeWeight(e);
    }

    public boolean removeAllEdges(Collection<? extends DirectedEdge<ExecutableMethod>> edges) {        
        return graph.removeAllEdges(edges);
    }

    public Set<DirectedEdge<ExecutableMethod>> removeAllEdges(ExecutableMethod sourceVertex,
            ExecutableMethod targetVertex) {        
        return graph.removeAllEdges(sourceVertex, targetVertex);
    }

    public boolean removeAllVertices(Collection<? extends ExecutableMethod> vertices) {        
        return graph.removeAllVertices(vertices);
    }

    public boolean removeEdge(DirectedEdge<ExecutableMethod> e) {
        return graph.removeEdge(e);
    }

    public DirectedEdge<ExecutableMethod> removeEdge(ExecutableMethod sourceVertex,
            ExecutableMethod targetVertex) {
        return graph.removeEdge(sourceVertex, targetVertex);
    }

    public boolean removeVertex(ExecutableMethod v) {        
        return graph.removeVertex(v);
    }

    public Set<ExecutableMethod> vertexSet() {        
        return graph.vertexSet();
    }
    
    public int inDegreeOf(ExecutableMethod arg0) {        
        return graph.inDegreeOf(arg0);
    }
    public Set<DirectedEdge<ExecutableMethod>> incomingEdgesOf(ExecutableMethod arg0) {        
        return graph.incomingEdgesOf(arg0);
    }
    public int outDegreeOf(ExecutableMethod arg0) {        
        return graph.outDegreeOf(arg0);
    }
    public Set<DirectedEdge<ExecutableMethod>> outgoingEdgesOf(ExecutableMethod arg0) {        
        return graph.outgoingEdgesOf(arg0);
    }

    /**
     * Filter incoming methods, finding {@link ExecutableMethod} and create graph.
     * 
     * @param methods {@link IOpenMethod}
     * @return {@link DependencyRulesGraph} graph representing dependencies between executable rules methods. 
     */
    public static DependencyRulesGraph filterAndCreateGraph(List<IOpenMethod> methods) {
        List<ExecutableMethod> rulesMethods = new ArrayList<ExecutableMethod>(); 
        if (methods != null && methods.size() > 0) {
            for (IOpenMethod method : methods) {
                if (method instanceof ExecutableMethod) {
                    rulesMethods.add((ExecutableMethod)method);
                }
            }
            return new DependencyRulesGraph(rulesMethods);
        } else {
            throw new IllegalArgumentException("There is no rules for building graph.");
        }
    }
    
    

}
