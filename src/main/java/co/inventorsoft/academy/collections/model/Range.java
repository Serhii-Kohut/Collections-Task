package co.inventorsoft.academy.collections.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

public class Range<T extends Comparable<T>> implements Set<T> {
    private T start;
    private T end;
    private Comparator<T> comparator;
    private Function<T, T> incrementor;



    public Range(T start, T end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end must be non-null");
        }

        this.start = start;
        this.end = end;
        this.comparator = Comparator.naturalOrder();
    }

    public Range(T start, T end, Comparator<T> comparator) {
        if (start == null || end == null || comparator == null) {
            throw new IllegalArgumentException("All arguments must be non-null");
        }

        this.start = start;
        this.end = end;
        this.comparator = comparator;
    }

    public Range(T start, T end, Function<T, T> incrementor) {
        if (start == null || end == null || incrementor == null) {
            throw new IllegalArgumentException("All arguments must be non-null");
        }

        this.start = start;
        this.end = end;
        this.incrementor = incrementor;
        this.comparator = Comparator.naturalOrder();
    }

    public static <T extends Comparable<T>> Range<T> of(T start, T end) {
        if (start != null && start.compareTo(end) > 0) {
            throw new IllegalArgumentException("start must be less than or equal to end");
        }
        return new Range<>(start, end, Comparator.naturalOrder());
    }

    public static <T extends Comparable<T>> Range<T> of(T start, T end, Function<T, T> incrementor) {
        if (start == null || end == null || incrementor == null) {
            throw new IllegalArgumentException("All arguments must be non-null");
        }
        return new Range<>(start, end, incrementor);
    }

    private T successor(T current) {
        if (current instanceof Integer) {
            return (T) (Integer) ((Integer) current + 1);
        } else if (current instanceof Long) {
            return (T) (Long) ((Long) current + 1L);
        } else if (current instanceof Character) {
            return (T) (Character) ((char) ((Character) current + 1));
        } else if (current instanceof Float) {
            return (T) Float.valueOf(((Float) current + 0.1f));

        }
        throw new UnsupportedOperationException("Unsupported type");
    }


    public T predecessor(T current) {
        if (current instanceof Integer) {
            return (T) (Integer) ((Integer) current - 1);
        } else if (current instanceof Float) {
            return (T) (Float) ((Float) current - 0.01f);
        } else if (current instanceof Double) {
            return (T) (Double) ((Double) current - 0.1);
        } else if (current instanceof Long) {
            return (T) (Long) ((Long) current - 1L);
        } else if (current instanceof Character) {
            return (T) (Character) ((char) ((Character) current - 1));
        } else {
            throw new IllegalStateException("Unsupported type: " + current.getClass().getName());
        }
    }


    public int size() {
        if (start == null || end == null) {
            return 0;
        }
        if (start.equals(end)) {
            return 1;
        }
        int count = 0;
        T current = start;
        while (comparator.compare(current, end) < 0) {
            count++;
            current = incrementor != null ? incrementor.apply(current) : successor(current);
        }
        if (comparator.compare(current, end) == 0) {
            count++;
        }
        return count;
    }

    public boolean isEmpty() {
        return start.equals(end);
    }

    public boolean contains(Object o) {
        T value = (T) o;
        return value.compareTo((T) start) >= 0 && value.compareTo((T) end) <= 0;
    }

    public Iterator<T> iterator() {
        if (start == null || end == null) {
            throw new NullPointerException("start and end must be non-null");
        }

        return new Iterator<T>() {
            private T current = (T) start;
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return comparator.compare(current, (T) end) <= 0;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T result = current;
                current = successor(current);
                currentIndex++;
                return result;
            }
        };
    }
    public Object[] toArray() {
        int size = size();
        Object[] array = new Object[size];

        Iterator<T> iterator = iterator();
        for (int i = 0; i < size; i++) {
            array[i] = iterator.next();
        }
        return array;
    }

    public <T1> T1[] toArray(T1[] a) {
        if (a == null) return null;

        int size = size();
        if (a.length < size) return (T1[]) Arrays.copyOf(a, size);

        Iterator<T> iterator = iterator();
        int i = 0;
        while (i < size) {
            a[i] = (T1) iterator.next();
            i++;
        }

        if (i < a.length) a[i] = null;

        return a;
    }

    public boolean add(T t) {
        if (t.compareTo((T) start) >= 0 && t.compareTo((T) end) <= 0) {
            if (t.equals(start)) {
                start = t;
            } else if (t.equals(end)) {
                end = t;
            } else {
                end = successor((T) end);
            }
            return true;
        } else {
            return false;
        }
    }


    public boolean remove(Object o) {
        if (o.equals(start)) {
            start = successor((T) start);
            return true;
        } else if (o.equals(end)) {
            end = predecessor((T) end);
            return true;
        } else {
            T previous = predecessor((T) o);
            if (previous == null) {
                return false;
            }

            if (o.equals(previous)) {
                end = predecessor(previous);
                return true;
            }
            throw new IllegalStateException("Object is not in the range");
        }
    }

    public boolean containsAll(Collection<?> c) {
        return c.stream()
                .allMatch(this::contains);
    }

    public boolean addAll(Collection<? extends T> c) {
        return c.stream()
                .anyMatch(this::add);
    }

    public boolean retainAll(Collection<?> c) {
        return this.stream()
                .filter(c::contains)
                .count() == this.size();
    }

    public boolean removeAll(Collection<?> c) {
        return this.stream()
                .noneMatch(c::contains);
    }

    public void clear() {
        for (T o : this) {
            this.remove(o);
        }
    }
}
