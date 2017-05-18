package com.coopstools;

import java.util.List;

public class Parent {

    private final String name;
    private List<Child> children;

    public Parent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Parent parent = (Parent) o;

        return getName().equals(parent.getName());

    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
