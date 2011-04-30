package com.monitorend;


import java.net.DatagramPacket;

class DatagramPackeCell {
	
	private DatagramPackeCell next;
	private DatagramPacket datagramPacket;
	
	public DatagramPackeCell(DatagramPacket datagramPacket) {
		this.datagramPacket = datagramPacket;
	}
	
	public DatagramPackeCell(DatagramPacket datagramPacket, DatagramPackeCell next) {
		this.datagramPacket = datagramPacket;
		this.next = next;
	}
	
	public DatagramPacket getDatagramPacket() {
		return datagramPacket;
	}
	
	public void setDatagramPacket(DatagramPacket datagramPacket) {
		this.datagramPacket = datagramPacket;
	}
	
	public DatagramPackeCell getNext() {
		return next;
	}
	
	public void setNext(DatagramPackeCell next) {
		this.next = next;
	}
}

class DatagramPacketQueue {
	
	private DatagramPackeCell head, tail;
	private boolean mIsFinished;
	
	public DatagramPacketQueue() {
		head = null;
		tail = null;
		mIsFinished = false;
	}
	
	public synchronized void add(DatagramPacket datagramPacket) {
		
		DatagramPackeCell p = new DatagramPackeCell(datagramPacket);
		if( tail == null ) {
			head = p;
		}
		else {
			tail.setNext(p);
		}
		
		p.setNext(null);
		tail = p;
		notifyAll();
	}
	
	public synchronized DatagramPacket take(){
		
		while( head == null && !mIsFinished) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("InterruptedException in DatagramPacketQueue");
				//e.printStackTrace();
			}
		}
		
		DatagramPackeCell p = head;
		head = head.getNext();
		if ( head == null ) {
			tail = null;
		}
		
		return p.getDatagramPacket();		
	}
	
	public synchronized void setFinished() {
		mIsFinished = true;
	}
}