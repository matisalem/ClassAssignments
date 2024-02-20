package org.example;

import java.util.*;

class BelferElevator {

    Queue<Integer> floorRequests = new PriorityQueue<>();
    ElevatorState state = ElevatorState.NOT_MOVING;
    private int currentFloor = 1;
    private int current_weight = 0;
    private Set<Integer> calledUp = new HashSet<>();
    private Set<Integer> calledDown = new HashSet<>();
    final int total_floors = 18;
    boolean empty = true;
    // up = 1, still = 0, down = -1
    int direction = 0;
    private DoublyLinkList myList = new DoublyLinkList();

    private int status_helper; 

    enum ElevatorState {
        NOT_MOVING,
        MOVING_UP,
        MOVING_DOWN,
        DOORS_OPEN,
    }

    public BelferElevator() {
        for (int i = 1; i <= 16; i++){
            myList.addLast(i);
        }
    }

    public void returnTo1(){
        if (calledUp.isEmpty() && calledDown.isEmpty()){
            while (calledUp.isEmpty() && calledDown.isEmpty() && currentFloor > 1){
                state = ElevatorState.MOVING_DOWN;
                currentFloor--;
            }
        }

        if (calledUp.isEmpty() && calledDown.isEmpty() && currentFloor == 1){
            state = ElevatorState.NOT_MOVING;
        }

    }

        public void move(int floorToGetTo) {
            if (floorToGetTo > currentFloor) {
                state = ElevatorState.MOVING_UP;
                while (!calledUp.isEmpty()) {
                    DoublyLinkList.ListNode node = myList.head;
                    if (calledUp.contains(node.next)) {
                        stop();
                        state = ElevatorState.MOVING_UP;
                    }
                    currentFloor++;
                    move(floorToGetTo);
                }

            } else if (floorToGetTo < currentFloor) {
                state = ElevatorState.MOVING_DOWN;
                while (!calledUp.isEmpty()) {
                    DoublyLinkList.ListNode node = myList.head;
                    if (calledDown.contains(node.prev)) {
                        stop ();
                        state = ElevatorState.MOVING_DOWN;
                    }
                    currentFloor--;
                    move(floorToGetTo);
                }

            }else{
                stop();
                return;
            }
    }

    public void setStatus(int i){
        if (i > 0){
            this.state = ElevatorState.MOVING_UP;
        } else if (i < 0){
            this.state = ElevatorState.MOVING_DOWN;
        } else {
            this.state = ElevatorState.NOT_MOVING;
        }
    }

    // region TINY METHODS

    public String stop(){
        return "you may be questioning why its returning a string, and thats a good question";
    }
    public boolean isEmpty() {
        return (current_weight == 0);
    }

    public int getFloor(){
        return currentFloor;
    }

    public void setCalledUpperFloor(int floor){
        this.calledUp.add(floor);
    }

    public void setCalledDownFloor(int floor){
        this.calledDown.add(floor);
    }

    public boolean isHighFloorPressed(int floor){
        return calledUp.contains(floor);
    }
    public boolean isLowFloorPressed(int floor){
        return calledUp.contains(floor);
    }

    public int goUp(){
        return 0;
    }

    public ElevatorState getStatus(){
        return this.state;
    }

    public void setStatus_helper(int i){
        this.status_helper = i;
    }

    public int getStatus_helper(){
       return this.status_helper;
    }

    public Set getCalledUpperFloors(){
        return this.calledUp;
    }

    public Set getCalledDownFloors(){
        return this.calledDown;
    }

    // endregion


    public class DoublyLinkList {
        private ListNode head;
        private ListNode tail;
        private int size;

        private class ListNode {
            int val;
            ListNode prev;
            ListNode next;

            public ListNode(int val) {
                this.val = val;
                this.prev = null;
                this.next = null;
            }
        }

        public DoublyLinkList() {
            this.head = null;
            this.tail = null;
            this.size = 0;
        }

        public void addFirst(int val) {
            ListNode newNode = new ListNode(val);
            if (isEmpty()) {
                tail = newNode;
            } else {
                head.prev = newNode;
            }
            newNode.next = head;
            head = newNode;
            size++;
        }

        public void addLast(int val) {
            ListNode newNode = new ListNode(val);
            if (isEmpty()) {
                head = newNode;
            } else {
                tail.next = newNode;
                newNode.prev = tail;
            }
            tail = newNode;
            size++;
        }

        public void removeFirst() {
            if (isEmpty()) {
                throw new NoSuchElementException();
            }
            if (head == tail) {
                head = tail = null;
            } else {
                head = head.next;
                head.prev = null;
            }
            size--;
        }

        public void removeLast() {
            if (isEmpty()) {
                throw new NoSuchElementException();
            }
            if (head == tail) {
                head = tail = null;
            } else {
                tail = tail.prev;
                tail.next = null;
            }
            size--;
        }

        public int getFirst() {
            if (isEmpty()) {
                throw new NoSuchElementException();
            }
            return head.val;
        }

        public int getLast() {
            if (isEmpty()) {
                throw new NoSuchElementException();
            }
            return tail.val;
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public int size() {
            return size;
        }
    }

}
