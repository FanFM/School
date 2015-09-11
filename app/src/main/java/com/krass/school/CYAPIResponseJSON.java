
package com.krass.school;

class StandardApiResponse {

	public String status;

	public String error;

	public String error_text;

}

class GetTicketResponse extends StandardApiResponse {

	public String ticket;

}

class LoginResponse extends StandardApiResponse {

}
