package com.scmspain.services;

import com.scmspain.entities.DiscardTweet;
import com.scmspain.entities.Tweet;
import com.scmspain.entities.TweetURL;
import com.scmspain.entities.URL;

import org.springframework.boot.actuate.metrics.writer.Delta;
import org.springframework.boot.actuate.metrics.writer.MetricWriter;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class TweetService {
	private EntityManager entityManager;
	private MetricWriter metricWriter;

	public TweetService(EntityManager entityManager, MetricWriter metricWriter) {
		this.entityManager = entityManager;
		this.metricWriter = metricWriter;
	}

	/**
	 * Push tweet to repository Parameter - publisher - creator of the Tweet
	 * Parameter - text - Content of the Tweet Result - recovered Tweet (https?|http).*?\\s
	 */
	public void publishTweet(String publisher, String text) {
		String wordToFind = "\\b(https?|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\\s";
		Pattern word = Pattern.compile(wordToFind);
		Matcher match = word.matcher(text);

		if (publisher != null && publisher.length() > 0 && text != null && text.replaceAll(wordToFind, "").length() > 0
				&& text.replaceAll(wordToFind, "").length() < 140) {
			TweetURL tweet = new TweetURL();
			tweet.setTweet(text.replaceAll(wordToFind, ""));
			tweet.setPublisher(publisher);

			List<URL> urlList = new ArrayList<URL>();
			while (match.find()) {
				URL url = new URL();
				url.setUrl(text.substring(match.start(), match.end()));
				url.setBegin(match.start());
				url.setTweetURL(tweet);
				urlList.add(url);
			}

			this.metricWriter.increment(new Delta<Number>("published-tweets", 1));

			if (urlList.size() > 0) {
				tweet.setURLs(urlList);
			}

			this.entityManager.persist(tweet);

		} else {
			throw new IllegalArgumentException("Tweet must not be greater than 140 characters");
		}
	}

	/**
	 * Discard a tweet by logical
	 * 
	 * @param text
	 *            is a Tweet ID
	 */
	public void discardTweet(String text) {

		if (text != null && !text.isEmpty()) {
			Tweet tweet = getTweet(Long.parseLong(text));
			DiscardTweet discard = new DiscardTweet();
			discard.setTweet(tweet);
			try {
				this.entityManager.persist(discard);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException("Tweet must be a valid ID");
		}
	}

	/**
	 * Recover tweet from repository Parameter - id - id of the Tweet to
	 * retrieve Result - retrieved Tweet
	 */
	public Tweet getTweet(Long id) {
		return this.entityManager.find(Tweet.class, id);
	}

	/**
	 * Recover tweetURL from repository Parameter - id - id of the TweetURL to
	 * retrieve Result - retrieved TweetURL
	 */
	public Tweet getTweetURL(Long id) {
		TweetURL tweet = this.entityManager.getReference(TweetURL.class, id);
		this.entityManager.detach(tweet);
		this.metricWriter.increment(new Delta<Number>("times-queried-tweets", 1));
		TypedQuery<TweetURL> query = this.entityManager.createQuery(
				"SELECT t FROM TweetURL t left join fetch t.URLs WHERE t.id = :id ORDER BY t.id DESC", TweetURL.class);
		query.setParameter("id", id);
		TweetURL turl = query.getSingleResult();

		Iterator iterator = turl.getURLs().iterator();
		while (iterator.hasNext()) {
			URL url = (URL) iterator.next();
			String text = turl.getTweet();
			text = new StringBuilder(text).insert(url.getBegin(), url.getUrl()).toString();
			tweet.setTweet(text);
		}
		Tweet t = new Tweet();
		t.setId(tweet.getId());
		t.setPre2015MigrationStatus(tweet.getPre2015MigrationStatus());
		t.setPublisher(tweet.getPublisher());
		t.setTweet(tweet.getTweet());
		return t;
	}

	/**
	 * Recover tweet from repository Parameter - id - id of the Tweet to
	 * retrieve Result - retrieved Tweet
	 */
	public List<Tweet> listAllTweets() {
		List<Tweet> result = new ArrayList<Tweet>();
		this.metricWriter.increment(new Delta<Number>("times-queried-tweets", 1));
		TypedQuery<Long> query = this.entityManager.createQuery(
				"SELECT id FROM Tweet AS tweetId WHERE pre2015MigrationStatus<>99 AND id not in (select tweet.id from DiscardTweet) ORDER BY id DESC",
				Long.class);

		List<Long> ids = query.getResultList();
		for (Long id : ids) {
			Tweet tweet = getTweetURL(id);
			if (tweet == null)
				result.add(getTweet(id));
			else
				result.add(tweet);
		}
		return result;
	}

	/**
	 * Recover discarded tweet from repository Parameter - id - id of the Tweet
	 * to retrieve Result - retrieved Tweet
	 */
	public List<Tweet> listAllDiscardedTweets() {
		List<Tweet> result = new ArrayList<Tweet>();
		this.metricWriter.increment(new Delta<Number>("times-queried-tweets", 1));
		TypedQuery<Long> query = this.entityManager.createQuery("select tweet.id from DiscardTweet ORDER BY 1 DESC",
				Long.class);

		List<Long> ids = query.getResultList();
		for (Long id : ids) {
			Tweet tweet = getTweetURL(id);
			if (tweet == null)
				result.add(getTweet(id));
			else
				result.add(tweet);
		}
		return result;
	}
}
