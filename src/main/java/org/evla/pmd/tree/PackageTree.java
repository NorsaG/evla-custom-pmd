package org.evla.pmd.tree;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class PackageTree {
    private static final String ROOT_STR = "root";
    private static final Node ROOT_NODE = new Node(ROOT_STR, null);

    private final Map<String, Node> nodes;

    public PackageTree() {
        nodes = new HashMap<>();
        nodes.put(ROOT_STR, ROOT_NODE);
    }

    public Set<Node> getRoots() {
        return new HashSet<>(nodes.values());
    }

    public void addNode(String pkgName) {
        addNodeValue(ROOT_NODE, pkgName);
    }

    public Set<Node> getAllLeafs() {
        Set<Node> set = new HashSet<>();
        iterateNodes(set, new HashSet<>(nodes.values()));

        return set;
    }

    public Set<Node> getAllLeafsForNode(Node node) {
        Set<Node> set = new HashSet<>();
        iterateNodes(set, node.getChildren());
        return set;
    }

    private void iterateNodes(Set<Node> destinationSet, Set<Node> nodes) {
        for (Node node : nodes) {
            if (node.isLeaf()) {
                destinationSet.add(node);
            }
            iterateNodes(destinationSet, node.getChildren());
        }
    }

    private void addNodeValue(Node node, String pkgName) {
        int pos = pkgName.indexOf(".");
        if (pos == -1) {
            node.addChildren(pkgName);
        } else {
            String currPkg = pkgName.substring(0, pos);
            node.addChildren(currPkg);

            String nextPkg = pkgName.substring(pos + 1);
            addNodeValue(node.getChild(currPkg), nextPkg);
        }
    }

    public static class Node {
        private final String name;
        private final Node parent;
        private final int depth;
        private final Map<String, Node> children;

        private Boolean isLeaf;

        public Node(String node, Node parent) {
            this.name = node;
            this.parent = parent;
            this.children = new HashMap<>();

            this.depth = parent == null ? 0 : parent.depth + 1;
            this.isLeaf = true;
        }

        public Node getChild(String childName) {
            return this.children.get(childName);
        }

        public String getFullName() {
            String parentStr = parent != null ? parent.getFullName() : "";
            return this != ROOT_NODE
                    ? StringUtils.isBlank(parentStr) ? name : parentStr + "." + name
                    : "";
        }

        public void addChildren(String child) {
            this.children.putIfAbsent(child, new Node(child, this));
            this.isLeaf = false;
        }

        public int getDepth() {
            return depth;
        }

        public String getNodeName() {
            return name;
        }

        public Set<Node> getChildren() {
            return new HashSet<>(children.values());
        }

        public Node getParent() {
            return parent != ROOT_NODE ? parent : null;
        }

        public boolean isLeaf() {
            return isLeaf;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Node node = (Node) o;
            return Objects.equals(this.name, node.name) && Objects.equals(this.parent, node.parent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.parent);
        }
    }
}
