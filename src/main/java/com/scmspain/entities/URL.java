package com.scmspain.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Proxy;

@Entity
@Proxy(lazy = false)
public class URL {
	@Id
    @GeneratedValue
    @Column(name = "url_id")
    private Long id;
	@Column(nullable = false)
    private String url;
	@Column(nullable = false)
	private Integer begin;
	@ManyToOne
	@JoinColumn(name = "id", nullable=true)
	private TweetURL tweetURL;
	
	
	public TweetURL getTweetURL() {
		return tweetURL;
	}
	public void setTweetURL(TweetURL tweetURL) {
		this.tweetURL = tweetURL;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Integer getBegin() {
		return begin;
	}
	public void setBegin(Integer begin) {
		this.begin = begin;
	}
	
}
