package com.scmspain.entities;


import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
@Entity
public class DiscardTweet implements Serializable{
	@Id
	@ManyToOne
	@JoinColumn(name = "id")
	private Tweet tweet;

	public Tweet getTweet() {
		return tweet;
	}

	public void setTweet(Tweet tweet) {
		this.tweet = tweet;
	}
	
}
