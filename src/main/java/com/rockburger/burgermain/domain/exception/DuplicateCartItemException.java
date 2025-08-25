package com.rockburger.burgermain.domain.exception;

public class DuplicateCartItemException extends RuntimeException {
	private final Long articleId;

	public DuplicateCartItemException(String message, Long articleId) {
		super(message);
		this.articleId = articleId;
	}

	public Long getArticleId() {
		return articleId;
	}
}