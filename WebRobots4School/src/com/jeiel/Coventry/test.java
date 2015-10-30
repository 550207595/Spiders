package com.jeiel.coventry;

import java.util.regex.Pattern;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("1-2year".replaceAll("[1-9](-[1-9]){0,1}year", "p"));
		System.out.println("1year".replaceAll("[1-9](-[1-9]){0,1}year", "p"));
		System.out.println("10-12month".replaceAll("[1-9][0-9](-[1-9][0-9]){0,1}month", "p"));
		System.out.println("10month".replaceAll("[1-9][0-9](-[1-9][0-9]){0,1}month", "p"));
		System.out.println("100-120week".replaceAll("[1-9][0-9]*(-[1-9][0-9]*){0,1}week", "p"));
		System.out.println("100week".replaceAll("[1-9][0-9]*(-[1-9][0-9]*){0,1}week", "p"));
		System.out.println("1 year full-time, 24-36 months part-time".replace(" ", "").replaceAll("[1-9][0-9](-[1-9][0-9]){0,1}month", "a"));
		String a="adfasdf";
		a.substring(3);
		System.out.println(a);
	}

}
