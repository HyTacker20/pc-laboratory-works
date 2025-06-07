package com.shapeeditor.util;

import java.util.ArrayList;
import java.util.List;

import com.shapeeditor.model.Shape;

/**
 * A QuadTree implementation for efficient spatial partitioning of shapes.
 * This data structure allows for fast querying of shapes that intersect with a point or region.
 */
public class QuadTree {
    /** Maximum number of shapes in a node before it splits */
    private static final int MAX_SHAPES = 10;
    
    /** Maximum depth of the quadtree */
    private static final int MAX_DEPTH = 8;
    
    /** The boundary of this quadtree node */
    private final Boundary boundary;
    
    /** The depth of this node in the tree */
    private final int depth;
    
    /** The shapes contained in this node (only if this is a leaf node) */
    private List<Shape> shapes;
    
    /** The four child nodes (null if this is a leaf node) */
    private QuadTree[] children;
    
    /**
     * Constructs a new QuadTree with the specified boundary and depth.
     *
     * @param boundary The boundary of this quadtree node
     * @param depth The depth of this node in the tree
     */
    public QuadTree(Boundary boundary, int depth) {
        this.boundary = boundary;
        this.depth = depth;
        this.shapes = new ArrayList<>();
        this.children = null;
    }
    
    /**
     * Inserts a shape into the quadtree.
     *
     * @param shape The shape to insert
     * @param shapeBounds The bounds of the shape [left, top, right, bottom]
     * @return true if the shape was inserted, false otherwise
     */
    public boolean insert(Shape shape, double[] shapeBounds) {
        // Check if the shape intersects with this node's boundary
        if (!boundary.intersects(shapeBounds)) {
            return false;
        }
        
        // If this node is a leaf and has space, add the shape
        if (children == null && shapes.size() < MAX_SHAPES) {
            shapes.add(shape);
            return true;
        }
        
        // If this node is a leaf but is full, split it
        if (children == null && depth < MAX_DEPTH) {
            split();
        }
        
        // If this node is not a leaf, try to insert into children
        if (children != null) {
            boolean inserted = false;
            for (QuadTree child : children) {
                if (child.insert(shape, shapeBounds)) {
                    inserted = true;
                }
            }
            return inserted;
        } else {
            // If we've reached max depth, just add to this node
            shapes.add(shape);
            return true;
        }
    }
    
    /**
     * Splits this node into four child nodes.
     */
    private void split() {
        double x = boundary.x;
        double y = boundary.y;
        double halfWidth = boundary.width / 2;
        double halfHeight = boundary.height / 2;
        
        // Create the four child nodes
        children = new QuadTree[4];
        children[0] = new QuadTree(new Boundary(x, y, halfWidth, halfHeight), depth + 1); // Top-left
        children[1] = new QuadTree(new Boundary(x + halfWidth, y, halfWidth, halfHeight), depth + 1); // Top-right
        children[2] = new QuadTree(new Boundary(x, y + halfHeight, halfWidth, halfHeight), depth + 1); // Bottom-left
        children[3] = new QuadTree(new Boundary(x + halfWidth, y + halfHeight, halfWidth, halfHeight), depth + 1); // Bottom-right
        
        // Redistribute shapes to children
        List<Shape> oldShapes = new ArrayList<>(shapes);
        shapes.clear();
        
        for (Shape shape : oldShapes) {
            double[] shapeBounds = getShapeBounds(shape);
            for (QuadTree child : children) {
                child.insert(shape, shapeBounds);
            }
        }
    }
    
    /**
     * Queries the quadtree for shapes that contain the specified point.
     *
     * @param x The x-coordinate of the point
     * @param y The y-coordinate of the point
     * @return A list of shapes that contain the point, in no particular order
     */
    public List<Shape> queryPoint(double x, double y) {
        List<Shape> result = new ArrayList<>();
        
        // Check if the point is within this node's boundary
        if (!boundary.contains(x, y)) {
            return result;
        }
        
        // If this is a leaf node, check all shapes
        if (children == null) {
            for (Shape shape : shapes) {
                if (shape.contains(x, y)) {
                    result.add(shape);
                }
            }
            return result;
        }
        
        // If this is not a leaf node, query the appropriate child
        for (QuadTree child : children) {
            result.addAll(child.queryPoint(x, y));
        }
        
        return result;
    }
    
    /**
     * Clears all shapes from the quadtree.
     */
    public void clear() {
        shapes.clear();
        if (children != null) {
            for (QuadTree child : children) {
                child.clear();
            }
            children = null;
        }
    }
    
    /**
     * Updates the quadtree with the current shapes.
     *
     * @param allShapes The list of all shapes
     */
    public void update(List<Shape> allShapes) {
        clear();
        for (Shape shape : allShapes) {
            insert(shape, getShapeBounds(shape));
        }
    }
    
    /**
     * Gets the bounds of a shape.
     *
     * @param shape The shape
     * @return The bounds of the shape [left, top, right, bottom]
     */
    private double[] getShapeBounds(Shape shape) {
        // This is a simplified implementation
        // In a real implementation, you would get the actual bounds from the shape
        double x = shape.getX();
        double y = shape.getY();
        
        // Assuming a default size for simplicity
        // In a real implementation, you would get the actual size from the shape
        double size = 50;
        
        return new double[] {x - size, y - size, x + size, y + size};
    }
    
    /**
     * A simple class representing a rectangular boundary.
     */
    public static class Boundary {
        /** The x-coordinate of the top-left corner */
        public final double x;
        
        /** The y-coordinate of the top-left corner */
        public final double y;
        
        /** The width of the boundary */
        public final double width;
        
        /** The height of the boundary */
        public final double height;
        
        /**
         * Constructs a new Boundary with the specified position and size.
         *
         * @param x The x-coordinate of the top-left corner
         * @param y The y-coordinate of the top-left corner
         * @param width The width of the boundary
         * @param height The height of the boundary
         */
        public Boundary(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        /**
         * Checks if this boundary contains the specified point.
         *
         * @param pointX The x-coordinate of the point
         * @param pointY The y-coordinate of the point
         * @return true if the point is contained within this boundary, false otherwise
         */
        public boolean contains(double pointX, double pointY) {
            return pointX >= x && pointX < x + width &&
                   pointY >= y && pointY < y + height;
        }
        
        /**
         * Checks if this boundary intersects with the specified rectangle.
         *
         * @param rect The rectangle [left, top, right, bottom]
         * @return true if the rectangle intersects with this boundary, false otherwise
         */
        public boolean intersects(double[] rect) {
            return rect[0] < x + width && rect[2] > x &&
                   rect[1] < y + height && rect[3] > y;
        }
    }
}