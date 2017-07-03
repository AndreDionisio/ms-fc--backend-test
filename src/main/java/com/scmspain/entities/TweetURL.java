package com.scmspain.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Proxy;
@Entity
@Proxy(lazy = false)
public class TweetURL extends Tweet{
	@Column(nullable = true)
	@OneToMany(fetch=FetchType.LAZY, mappedBy = "tweetURL",cascade=CascadeType.PERSIST)
	private List<URL> URLs;

	public List<URL> getURLs() {
		return URLs;
	}
	
	public void setURLs(List<URL> uRLs) {
		URLs = uRLs;
	}

}
