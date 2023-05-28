package org.evla.pmd;

import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.properties.StringProperty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PackageCouplingRule extends AbstractJavaRule {
    private static final String TEMPLATE = String.join(File.separator, "src", "main", "java");

    private static final StringProperty CLASS_NAME = new StringProperty("className", "Regular expression", "",
            1.0f);

    public PackageCouplingRule() {
        definePropertyDescriptor(CLASS_NAME);
    }

    @Override
    public Object visit(ASTClassOrInterfaceDeclaration node, Object data) {
        if (node.getTypeKind() == ASTAnyTypeDeclaration.TypeKind.CLASS) {
            String currentClass = node.getQualifiedName().getBinaryName();
            String inputClass = this.getProperty(CLASS_NAME);
            if (Objects.equals(currentClass, inputClass)) {
                List<String> classes = getAllClasses();
                PackageTree pkgTree = new PackageTree();
                classes.forEach(pkgTree::addNode);

                validateRoots(node, data, pkgTree);
                validateLeafs(node, data, pkgTree);
            }
        }
        return data;
    }

    private void validateRoots(ASTClassOrInterfaceDeclaration node, Object data, PackageTree packageTree) {
        Set<PackageTree.Node> roots = packageTree.getRoots();
        countRoots(node, data, roots);
    }

    private void countRoots(ASTClassOrInterfaceDeclaration node, Object data, Set<PackageTree.Node> roots) {
        if (roots.size() >= 3) {
            String message = "Too much package roots:\n" +
                    roots.stream()
                            .map(PackageTree.Node::getNodeName)
                            .collect(Collectors.joining("\n"));
            addViolation(data, node, message);
        }
    }

    private List<String> getAllClasses() {
        List<String> files = new ArrayList<>();
        List<String> classes;
        try (Stream<Path> stream = Files.walk(Paths.get(TEMPLATE))) {
            stream.filter(Files::isRegularFile).forEach(p -> files.add(p.toString()));

            classes = files
                    .stream()
                    .map(s -> s.substring(TEMPLATE.length() + 1))
                    .map(s -> s.replace(File.separator, "."))
                    .map(s -> s.substring(0, s.length() - 5))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return classes;
    }

    private void validateLeafs(ASTClassOrInterfaceDeclaration node, Object data, PackageTree packageTree) {
        Set<PackageTree.Node> leafs = packageTree.getAllLeafs();
        Set<PackageTree.Node> parents = leafs.stream().filter(n -> n.getDepth() > 1).map(PackageTree.Node::getParent).collect(Collectors.toSet());
        Set<PackageTree.Node> grandParents = parents.stream().filter(n -> n.getDepth() > 1).map(PackageTree.Node::getParent).collect(Collectors.toSet());

        HashMap<String, Set<PackageTree.Node>> firstLevelIntersection = new HashMap<>();
        HashMap<String, Set<PackageTree.Node>> secondLevelIntersection = new HashMap<>();

        checkIntersection(firstLevelIntersection, leafs, parents);
        checkIntersection(secondLevelIntersection, parents, grandParents);

        for (String pkgName : firstLevelIntersection.keySet()) {
            if (firstLevelIntersection.get(pkgName).size() > 2) {
                String message = "There is complexity in module/packages design:\n" +
                        firstLevelIntersection.get(pkgName).stream().map(PackageTree.Node::getFullName).collect(Collectors.joining("\n"));
                addViolation(data, node, message);
            }
            if (secondLevelIntersection.containsKey(pkgName)) {
                Set<PackageTree.Node> set = firstLevelIntersection.get(pkgName);
                set.addAll(secondLevelIntersection.get(pkgName));
                String message = "There is complexity in module/packages design:\n" +
                        set.stream().map(PackageTree.Node::getFullName).collect(Collectors.joining("\n"));
                addViolation(data, node, message);
            }
        }
    }

    private void checkIntersection(HashMap<String, Set<PackageTree.Node>> container, Set<PackageTree.Node> set1, Set<PackageTree.Node> set2) {
        for (PackageTree.Node first : set1) {
            for (PackageTree.Node second : set2) {
                if (Objects.equals(first.getNodeName(), second.getNodeName())) {
                    container.putIfAbsent(first.getNodeName(), new HashSet<>());
                    container.get(first.getNodeName()).add(first);
                    container.get(first.getNodeName()).add(second);
                }
            }
        }
    }

}
