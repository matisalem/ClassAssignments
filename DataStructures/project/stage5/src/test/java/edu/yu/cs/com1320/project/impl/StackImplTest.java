package edu.yu.cs.com1320.project.impl;
import org.junit.Test;
import java.net.URISyntaxException;
import edu.yu.cs.com1320.project.stage5.*;
import edu.yu.cs.com1320.project.stage5.impl.*;
import static edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat.BINARY;
import static edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat.TXT;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.net.URI;

public class StackImplTest {

    // 4 TESTS

    @Test
    public void pushSize() throws URISyntaxException, IOException {
        StackImpl<Integer> stack = new StackImpl<>();

        stack.push(1);
        stack.push(2);
        stack.push(3);
        stack.push(4);
        stack.push(5);

        assertEquals(stack.size(), 5);
    }

    @Test
    public void sizePopSize() throws URISyntaxException, IOException {
        StackImpl<Integer> stack = new StackImpl<>();

        stack.push(1);
        stack.push(2);
        stack.push(3);
        stack.push(4);
        stack.push(5);

        assertEquals(stack.size(), 5);

        stack.pop();
        stack.pop();

        assertEquals(stack.size(), 3);

        stack.push(5);

        assertEquals(stack.size(), 4);
    }

    @Test
    public void sizeIs0() throws URISyntaxException, IOException {
        StackImpl<Integer> stack = new StackImpl<>();

        stack.push(1);
        stack.push(2);
        stack.push(3);

        assertEquals(stack.size(), 3);

        stack.pop();
        stack.pop();
        stack.pop();

        assertEquals(stack.size(), 0);
    }

    @Test
    public void peekTest() throws URISyntaxException, IOException {
        StackImpl<Integer> stack = new StackImpl<>();

        stack.push(1);
        stack.push(2);
        stack.push(3);

        assertEquals(stack.peek() == stack.peek(), true);
        assertEquals(stack.peek() == 3, true);

        stack.pop();

        assertEquals(stack.peek() == stack.peek(), true);
        assertEquals(stack.peek() == 2, true);

        stack.pop();

        assertEquals(stack.peek() == stack.peek(), true);
        assertEquals(stack.peek() == 1, true);

        stack.pop();

        assertEquals(stack.pop() == null, true);


        assertEquals(stack.peek(), null);


        assertEquals(stack.size(), 0);
    }

}
