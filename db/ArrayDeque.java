package db;

public class ArrayDeque<Item> {

    //This implementation of the array double-ended queue uses a circular topology, hence the
    //use of the modulo operator for indices of underlying array.

    //The size of the array deque cached.
    private int size;

    //Declares the array that acts as the underlying data structure for the deque
    private Item[] array;

    //Declares the first position - the index of the underlying array that is the zero index of
    //the deque, the last position - the index of the array that is the last index of the deque
    private int nextFirst, nextLast;

    //Lower bound for underlying array length. Every array for this deque will be at least this size
    private static int minArrayLength = 8;

    //Rescale-factor used to geometrically rescale array; the new length of the array is
    //the factor multiplied by the old length.
    private static int RFACTOR = 2;

    //Threshold usage-ratio; if usage-ratio, defined by (array.length / size), falls under this
    // threshold, the array is rescaled to the current size multiplied by this threshold
    private static double THRESHOLD_USAGE_RATIO = 0.25;

    /**
     * Public constructor to create an empty array deque. Start with an array of Items of size 8
     */
    public ArrayDeque() {
        size = 0;
        array = (Item[]) new Object[minArrayLength];
        nextLast = Math.floorDiv(array.length, 2);
        nextFirst = nextLast - 1;
    }

    /**
     * Helper method called by adding methods to perform resizing operations
     */
    private void arrayEnlarge() {
        Item[] newArray = (Item[]) new Object[array.length * RFACTOR];
        System.arraycopy(array, 0, newArray, 0, nextLast);
        //Calculates number of remaining elements in deque starting from first to
        // the back of the array
        int firstToLast = array.length - nextFirst - 1;
        //Calculates the index of new array to start copying remaining elements
        int startCopyRestIndex = newArray.length - firstToLast;
        System.arraycopy(array, nextFirst + 1, newArray, startCopyRestIndex, firstToLast);
        array = newArray;
        nextFirst = Math.floorMod(startCopyRestIndex - 1, array.length);
    }

    /**
     * Adds item to the front of the array deque; resizes array if necessary
     */
    public void addFirst(Item item) {
        //Invariant: adding an item always increases size by 1
        size += 1;
        if (nextFirst == nextLast) {
            //In this case, note that adding an item to the front will result in a full
            // array (since it starts with first being before last) so we perform
            // resizing operations
            arrayEnlarge();
        }
        array[nextFirst] = item;
        nextFirst = Math.floorMod(nextFirst - 1, array.length);
    }

    /**
     * Adds item to the end of the array deque
     */
    public void addLast(Item item) {
        size += 1;
        if (nextLast == nextFirst) {
            //Similar to addFirst, in this case, the array is full, so resize
            arrayEnlarge();
        }
        array[nextLast] = item;
        nextLast = (nextLast + 1) % array.length;
    }

    /**
     * Checks if the deque is empty by checking if its size is 0
     */
    public boolean isEmpty() {
        return (size == 0);
    }

    /**
     * Public selector method to return deque's size
     */
    public int size() {
        return size;
    }

    /**
     * Prints the elements of the deque from first to last separated by a space
     */
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        } System.out.println("");
    }

    /**
     * Helper method called by remove methods to shrink array in order to save memory,
     * utilizes get method
     */
    private void arrayShrink() {
        //Creates a new array with the length of the old array times 1/RFACTOR
        int newArrayLength = Math.floorDiv(array.length, RFACTOR);
        Item[] newArray = (Item[]) new Object[newArrayLength];
        //This will be the new first position in the array; arbitrarily chosen so that items would
        //primarily lie in the center of the new array
        int newFirst = Math.floorDiv(newArrayLength, 4);
        int startCopyIndex = newFirst + 1;
        int i = 0;
        //Fills in new array with corresponding items
        while (i <= size) {
            newArray[startCopyIndex] = get(i);
            i = (i + 1) % array.length;
            startCopyIndex = (startCopyIndex + 1) % array.length;
        }
        array = newArray;
        nextFirst = newFirst;
        nextLast = (newFirst + size + 2) % array.length;
    }

    /**
     * Removes the first item in the deque and returns it; resizes underlying array if necessary
     */
    public Item removeFirst() {
        if (isEmpty()) {
            return null;
        }
        //Invariant; removing an item always decreases deque size by 1
        size -= 1;
        //Checks if deque size is a sufficiently small fraction of underlying array length
        // for resize operations. Also gives lower bound to array length so we don't shrink
        // for already-small arrays
        if (size <= array.length * THRESHOLD_USAGE_RATIO && size >= minArrayLength) {
            //If sufficient amount of underlying array is not being used, free up resources by
            //shrinking array
            arrayShrink();
        }
        nextFirst = (nextFirst + 1) % array.length;
        Item removed = array[nextFirst];
        array[nextFirst] = null;
        return removed;
    }

    /**
     * Removes the last item in the deque and returns it
     */
    public Item removeLast() {
        if (isEmpty()) {
            return null;
        }
        size -= 1;
        if (size <= array.length * THRESHOLD_USAGE_RATIO && size >= minArrayLength) {
            arrayShrink();
        }
        nextLast = Math.floorMod(nextLast - 1, array.length);
        Item removed = array[nextLast];
        array[nextLast] = null;
        return removed;
    }

    /**
     * Returns the item in the deque at position index in the array
     */
    public Item get(int index) {
        //Because first is the zero index, we count the indices from there
        if (index < 0 || index >= size) {
            return null;
        }
        int arrayIndex = (nextFirst + index + 1) % array.length;
        return array[arrayIndex];
    }
}
