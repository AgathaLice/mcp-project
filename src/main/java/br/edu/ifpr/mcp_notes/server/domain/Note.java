package br.edu.ifpr.mcp_notes.server.domain;

/*
 * A Nota. A IA recomendou usar "records", por que dão um contructor, accessors, equals,
 * hashCode e toString como uma feature do Java. Foi meio confuso de entender
 */
public record Note(long id, String title, String content) {}
