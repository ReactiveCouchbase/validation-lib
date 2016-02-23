package org.reactivecouchbase.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Paths {

    private Paths() {
    }

    private static <T> List<T> empty(Class<T> clazz) {
        return new ArrayList<>();
    }

    public interface PathNode {
        String stringify();
    }

    public static class KeyPathNode implements PathNode {
        public final String key;

        public KeyPathNode(String key) {
            this.key = key;
        }

        @Override
        public String stringify() {
            return key;
        }

        @Override
        public String toString() {
            return stringify();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof KeyPathNode)) {
                return false;
            }

            KeyPathNode that = (KeyPathNode) o;

            return key.equals(that.key);

        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

    public static class IndexPathNode implements PathNode {
        public final int index;

        public IndexPathNode(int index) {
            this.index = index;
        }

        @Override
        public String stringify() {
            return "[" + index + "]";
        }

        @Override
        public String toString() {
            return stringify();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof IndexPathNode)) {
                return false;
            }

            IndexPathNode that = (IndexPathNode) o;

            return index == that.index;

        }

        @Override
        public int hashCode() {
            return index;
        }
    }

    public static final Path Root = new Path(empty(PathNode.class));

    private static final Pattern fieldArraySelector = Pattern.compile("(.+)\\[(\\d)+\\]");

    private static final Pattern arraySelector = Pattern.compile("\\[(\\d)+\\]");

    private static final Pattern dotSplitter = Pattern.compile("\\.");

    private static final Pattern squareBracketSplitter = Pattern.compile("\\[");

    public static Path parse(String query) {
        List<PathNode> pathes = new ArrayList<PathNode>();
        try {
            String[] partsArray = dotSplitter.split(query);
            List<String> parts = partsArray == null ? new ArrayList<String>() : Arrays.asList(partsArray);
            for (String part : parts) {
                if (fieldArraySelector.matcher(part).matches()) {
                    String[] subParts = squareBracketSplitter.split(part);
                    String field = subParts[0];
                    Integer index = Integer.valueOf(subParts[1].replace("]", ""));
                    pathes.add(new KeyPathNode(field));
                    pathes.add(new IndexPathNode(index));
                } else if (part.startsWith("[") && arraySelector.matcher(part).matches()) {
                    Integer index = Integer.valueOf(part.replace("[", "").replace("]", ""));
                    pathes.add(new IndexPathNode(index));
                } else {
                    pathes.add(new KeyPathNode(part));
                }
            }
        } catch (Exception e) {
            return Paths.Root;
        }
        return new Path(pathes);
    }

    public static class Path {
        public final List<PathNode> path;

        public Path(List<PathNode> path) {
            this.path = path;
        }

        public Path atIndex(int idx) {
            return andThen(idx);
        }

        public Path field(String k) {
            return andThen(k);
        }

        public Path andThen(int index) {
            List<PathNode> p = new ArrayList<PathNode>(path);
            p.add(new IndexPathNode(index));
            return new Path(p);
        }

        public Path andThen(String key) {
            List<PathNode> p = new ArrayList<PathNode>(path);
            p.add(new KeyPathNode(key));
            return new Path(p);
        }

        public Path compose(Path other) {
            List<PathNode> p = new ArrayList<PathNode>(path);
            p.addAll(other.path);
            return new Path(p);
        }

        @Override
        public String toString() {
            if (path.isEmpty()) {
                return "/";
            }
            return "/ " + path.stream().map(Object::toString).collect(Collectors.joining(" / "));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Path)) {
                return false;
            }
            Path path1 = (Path) o;
            return !(path != null ? !path.equals(path1.path) : path1.path != null);
        }

        @Override
        public int hashCode() {
            return path != null ? path.hashCode() : 0;
        }
    }
}
