package com.demo.chatservice.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.demo.chatservice.chat.ChatMessage;
import com.demo.chatservice.chat.MessageType;
import com.demo.chatservice.exception.FriendNotFoundException;
import com.demo.chatservice.external.FriendClient;
import com.demo.chatservice.external.UserClient;
import com.demo.chatservice.external.WatchlistClient;
import com.demo.chatservice.external.MovieClient;
import com.demo.chatservice.dto.WatchlistMovieDTO;
import com.demo.chatservice.dto.MovieDTO;
import com.demo.chatservice.repository.ChatMessageRepository;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@Slf4j
public class ChatController {
	
	private final ChatMessageRepository chatMessageRepository;
	
	private final FriendClient friendClient;
	
	private final SimpMessagingTemplate messagingTemplate;
	
	private final UserClient userClient;
	
	private final WatchlistClient watchlistClient;
	
	private final MovieClient movieClient;
	
	private final RestTemplate restTemplate = new RestTemplate();
	
	private static final String QUEUE = "/queue/messages";
	
	private static final String ASSISTANT = "AI Assistant";

	@MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
		log.info(chatMessage.getRecipient() + " " + chatMessage.getSender());

		if (chatMessage.getType() == MessageType.AI_REQUEST) {
			handleAIRequest(chatMessage);
			return;
		}
		
		String senderEmail = chatMessage.getSender();
	    String recipientEmail = chatMessage.getRecipient();

		int senderId = userClient.getUserIdByEmail(senderEmail);
		int recipientId = userClient.getUserIdByEmail(recipientEmail);
		
		boolean areFriends = friendClient.areFriends(senderId, recipientId);
		
		if(!areFriends) {
			throw new FriendNotFoundException("Not Friends!");
		}
		
		chatMessageRepository.save(chatMessage);
		
		// Send to recipient's private queue
        messagingTemplate.convertAndSendToUser(
        	chatMessage.getRecipient(),
            QUEUE,
            chatMessage
        );
        
        // Optionally, also send to sender for echo
        messagingTemplate.convertAndSendToUser(
            chatMessage.getSender(),
            QUEUE,
            chatMessage
        );
    }
    
    private void handleAIRequest(ChatMessage aiRequest) {
    	// Create a proper AI request message to display
    	ChatMessage displayRequest = ChatMessage.builder()
    		.type(MessageType.AI_REQUEST)
    		.content(aiRequest.getContent())
    		.sender(aiRequest.getSender())
    		.recipient(aiRequest.getRecipient())
    		.build();
    	
    	// Send the AI request to both users
    	messagingTemplate.convertAndSendToUser(
    		aiRequest.getSender(), QUEUE, displayRequest);
    	messagingTemplate.convertAndSendToUser(
    		aiRequest.getRecipient(), QUEUE, displayRequest);
    	
    	log.info("Processing AI request: '{}'", aiRequest.getContent());
    	
    	// Check if this is a watchlist request
    	if (isWatchlistRequest(aiRequest.getContent())) {
    		log.info("Detected as watchlist request");
    		Thread.startVirtualThread(() -> {
    			try {
    				Thread.sleep(2500);
    				String watchlistResponse = handleWatchlistRequestWithRetry(aiRequest.getSender());
    				
    				ChatMessage responseMessage = ChatMessage.builder()
    					.type(MessageType.AI_RESPONSE)
    					.content(watchlistResponse)
    					.sender(ASSISTANT)
    					.recipient(aiRequest.getSender())
    					.build();
    				
    				messagingTemplate.convertAndSendToUser(
    					aiRequest.getSender(), QUEUE, responseMessage);
    				messagingTemplate.convertAndSendToUser(
    					aiRequest.getRecipient(), QUEUE, responseMessage);
    			} catch (InterruptedException e) {
    				Thread.currentThread().interrupt();
    			}
    		});
    	} else if (isRecommendationRequest(aiRequest.getContent()) || isAIRecommendationRequest(aiRequest.getContent())) {
    		log.info("Detected as recommendation request");
    		Thread.startVirtualThread(() -> {
    			try {
    				Thread.sleep(3000);
    				String recommendationResponse = handleRecommendationRequestWithRetry(aiRequest.getSender(), aiRequest.getContent());
    				
    				ChatMessage responseMessage = ChatMessage.builder()
    					.type(MessageType.AI_RESPONSE)
    					.content(recommendationResponse)
    					.sender(ASSISTANT)
    					.recipient(aiRequest.getSender())
    					.build();
    				
    				messagingTemplate.convertAndSendToUser(
    					aiRequest.getSender(), QUEUE, responseMessage);
    				messagingTemplate.convertAndSendToUser(
    					aiRequest.getRecipient(), QUEUE, responseMessage);
    			} catch (InterruptedException e) {
    				Thread.currentThread().interrupt();
    			}
    		});
    	} else {
    		log.info("Detected as general AI request");
    		// Call AI service for general requests
    		Thread.startVirtualThread(() -> {
    			try {
    				String aiResponse = callAIServiceWithRetry(aiRequest.getContent());
    				
    				// Create AI response message
    				ChatMessage responseMessage = ChatMessage.builder()
    					.type(MessageType.AI_RESPONSE)
    					.content(aiResponse)
    					.sender(ASSISTANT)
    					.recipient(aiRequest.getSender())
    					.build();
    				
    				// Send AI response back to both users in the chat
    				messagingTemplate.convertAndSendToUser(
    					aiRequest.getSender(), QUEUE, responseMessage);
    				messagingTemplate.convertAndSendToUser(
    					aiRequest.getRecipient(), QUEUE, responseMessage);
    			} catch (Exception e) {
    				log.error("Error in general AI request: ", e);
    				sendErrorMessage(aiRequest, "Sorry, I couldn't process your request at the moment.");
    			}
    		});
    	}
    }
    
    private boolean isWatchlistRequest(String content) {
    	String lowerContent = content.toLowerCase();
    	return lowerContent.contains("watchlist") || lowerContent.contains("my movies") || 
    		   lowerContent.contains("show movies") || lowerContent.contains("display movies");
    }
    
    private boolean isRecommendationRequest(String content) {
    	String lowerContent = content.toLowerCase();
    	boolean isRecommendation = lowerContent.contains("recommend") || 
    						   lowerContent.contains("suggestion") || 
    						   lowerContent.contains("similar movies") || 
    						   lowerContent.contains("similar") || 
    						   lowerContent.contains("movie recommendations") ||
    						   lowerContent.contains("movie suggestions");
    	log.info("Checking if '{}' is recommendation request: {}", content, isRecommendation);
    	return isRecommendation;
    }
    
    private boolean isAIRecommendationRequest(String content) {
    	String lowerContent = content.toLowerCase();
    	return lowerContent.contains("ai recommend") || lowerContent.contains("external movies") || 
    		   lowerContent.contains("popular movies") || lowerContent.contains("new movies");
    }
    
    private String handleWatchlistRequestWithRetry(String userEmail) {
    	for (int attempt = 1; attempt <= 3; attempt++) {
    		try {
    			log.info("Watchlist request attempt {} for user: {}", attempt, userEmail);
    			int userId = userClient.getUserIdByEmail(userEmail);
    			List<WatchlistMovieDTO> watchlist = watchlistClient.getWatchListsByUser(userId);
    			
    			if (watchlist.isEmpty()) {
    				return "Your watchlist is empty. Add some movies to get started!";
    			}
    			
    			StringBuilder response = new StringBuilder("Here are the movies in your watchlist:\n\n");
    			for (WatchlistMovieDTO item : watchlist) {
    				if (item.getMovies() != null && !item.getMovies().isEmpty()) {
    					item.getMovies().forEach(movie -> {
    						response.append("🎬 ").append(movie.getTitle())
    							.append(" (").append(movie.getReleaseYear()).append(")\n")
    							.append("   Genre: ").append(movie.getGenre()).append("\n")
    							.append("   Status: ").append(item.getStatus()).append("\n");
    						if (item.getNote() != null && !item.getNote().isEmpty()) {
    							response.append("   Note: ").append(item.getNote()).append("\n");
    						}
    						response.append("\n");
    					});
    				}
    			}
    			
    			return response.toString();
    		} catch (Exception e) {
    			log.error("Watchlist request attempt {} failed: {}", attempt, e.getMessage());
    			if (attempt < 3) {
    				try {
    					Thread.sleep(1000 * attempt); // Progressive delay
    				} catch (InterruptedException ie) {
    					Thread.currentThread().interrupt();
    					return "Request interrupted.";
    				}
    			}
    		}
    	}
    	return "Sorry, I couldn't retrieve your watchlist after multiple attempts.";
    }
    
    private String handleRecommendationRequestWithRetry(String userEmail, String originalRequest) {
    	for (int attempt = 1; attempt <= 3; attempt++) {
    		try {
    			log.info("Recommendation request attempt {} for user: {}", attempt, userEmail);
    			
    			int userId = userClient.getUserIdByEmail(userEmail);
    			log.info("Retrieved userId: {}", userId);
    			
    			List<WatchlistMovieDTO> watchlist = watchlistClient.getWatchListsByUser(userId);
    			log.info("Retrieved watchlist with {} items", watchlist.size());
    			
    			if (watchlist.isEmpty()) {
    				return "Your watchlist is empty. Add some movies first to get personalized recommendations!";
    			}
    			
    			if (isAIRecommendationRequest(originalRequest)) {
    				log.info("Processing AI-only recommendation request");
    				return getAIRecommendations(watchlist);
    			}
    			
    			log.info("Processing combined recommendation request (database + AI)");
    			return getCombinedRecommendations(watchlist);
    			
    		} catch (Exception e) {
    			log.error("Recommendation request attempt {} failed: {}", attempt, e.getMessage());
    			if (attempt < 3) {
    				try {
    					Thread.sleep(1000 * attempt); // Progressive delay
    				} catch (InterruptedException ie) {
    					Thread.currentThread().interrupt();
    					return "Request interrupted.";
    				}
    			}
    		}
    	}
    	return "Sorry, I couldn't generate recommendations after multiple attempts.";
    }
    
    private List<MovieDTO> getDatabaseRecommendations(List<WatchlistMovieDTO> watchlist) {
    	try {
    		log.info("Fetching all movies from database");
    		List<MovieDTO> allMovies = movieClient.getAllMovies();
    		log.info("Retrieved {} movies from database", allMovies.size());
    		
    		Set<String> watchlistGenres = new HashSet<>();
    		Set<Integer> watchlistMovieIds = new HashSet<>();
    		
    		extractWatchlistData(watchlist, watchlistGenres, watchlistMovieIds);
    		log.info("Extracted genres: {} and movie IDs: {}", watchlistGenres, watchlistMovieIds);
    		
    		List<MovieDTO> recommendations = allMovies.stream()
    			.filter(movie -> !watchlistMovieIds.contains(movie.getMovieId()))
    			.filter(movie -> watchlistGenres.contains(movie.getGenre().toLowerCase()))
    			.limit(5)
    			.toList();
    		
    		log.info("Generated {} database recommendations", recommendations.size());
    		return recommendations;
    	} catch (Exception e) {
    		log.error("Error in getDatabaseRecommendations: {}", e.getMessage(), e);
    		return List.of();
    	}
    }
    
    private void extractWatchlistData(List<WatchlistMovieDTO> watchlist, Set<String> genres, Set<Integer> movieIds) {
    	for (WatchlistMovieDTO item : watchlist) {
    		if (item.getMovies() != null) {
    			for (MovieDTO movie : item.getMovies()) {
    				genres.add(movie.getGenre().toLowerCase());
    				movieIds.add(movie.getMovieId());
    			}
    		}
    	}
    }
    
    private String formatDatabaseRecommendations(List<MovieDTO> recommendations) {
    	StringBuilder response = new StringBuilder("Based on your watchlist, here are some movie recommendations from our database:\n\n");
    	for (MovieDTO movie : recommendations) {
    		response.append("🎆 ").append(movie.getTitle())
    			.append(" (").append(movie.getReleaseYear()).append(")\n")
    			.append("   Genre: ").append(movie.getGenre()).append("\n");
    		if (movie.getDirector() != null && !movie.getDirector().isEmpty()) {
    			response.append("   Director: ").append(movie.getDirector()).append("\n");
    		}
    		if (movie.getRating() > 0) {
    			response.append("   Rating: ").append(movie.getRating()).append("/10\n");
    		}
    		response.append("\n");
    	}
    	return response.toString();
    }
    
    private String getCombinedRecommendations(List<WatchlistMovieDTO> watchlist) {
    	StringBuilder response = new StringBuilder();
    	
    	try {
    		// Get database recommendations first
    		List<MovieDTO> databaseRecommendations = getDatabaseRecommendations(watchlist);
    		log.info("Found {} database recommendations", databaseRecommendations.size());
    		
    		if (!databaseRecommendations.isEmpty()) {
    			response.append("**Movies from our database similar to your watchlist:**\n\n");
    			for (MovieDTO movie : databaseRecommendations) {
    				response.append("🎬 ").append(movie.getTitle())
    					.append(" (").append(movie.getReleaseYear()).append(")\n")
    					.append("   Genre: ").append(movie.getGenre()).append("\n");
    				if (movie.getDirector() != null && !movie.getDirector().isEmpty()) {
    					response.append("   Director: ").append(movie.getDirector()).append("\n");
    				}
    				if (movie.getRating() > 0) {
    					response.append("   Rating: ").append(movie.getRating()).append("/10\n");
    				}
    				response.append("\n");
    			}
    			response.append("\n---\n\n");
    		}
    		
    		// Get AI recommendations
    		log.info("Starting AI recommendations call");
    		try {
    			String aiRecommendations = getAIRecommendations(watchlist);
    			log.info("AI recommendations received: {}", aiRecommendations.substring(0, Math.min(100, aiRecommendations.length())));
    			response.append(aiRecommendations);
    		} catch (Exception aiError) {
    			log.error("AI recommendations failed: {}", aiError.getMessage(), aiError);
    			response.append("**Popular movie recommendations from around the world:**\n\n");
    			response.append("🌟 The Shawshank Redemption (1994) - Drama - A banker convicted of uxoricide forms a friendship over a quarter century with a hardened convict\n");
    			response.append("🌟 The Godfather (1972) - Crime/Drama - The aging patriarch of an organized crime dynasty transfers control to his reluctant son\n");
    			response.append("🌟 Pulp Fiction (1994) - Crime/Drama - The lives of two mob hitmen, a boxer, and others intertwine in four tales of violence\n");
    			response.append("🌟 The Dark Knight (2008) - Action/Crime - Batman faces the Joker, a criminal mastermind who wants to plunge Gotham City into anarchy\n");
    			response.append("🌟 Forrest Gump (1994) - Drama/Romance - A man with low IQ accomplishes great things and influences popular culture\n\n");
    			response.append("*Note: AI service temporarily unavailable, showing popular classics instead.*");
    		}
    		
    		return response.toString();
    		
    	} catch (Exception e) {
    		log.error("Error getting combined recommendations: {}", e.getMessage(), e);
    		return "Sorry, I couldn't generate recommendations at the moment. Error: " + e.getMessage();
    	}
    }
    
    private String getAIRecommendations(List<WatchlistMovieDTO> watchlist) {
    	try {
    		log.info("Building AI recommendation request");
    		
    		// Build watchlist summary for AI
    		StringBuilder watchlistSummary = new StringBuilder("User's watchlist movies: ");
    		for (WatchlistMovieDTO item : watchlist) {
    			if (item.getMovies() != null) {
    				for (MovieDTO movie : item.getMovies()) {
    					watchlistSummary.append(movie.getTitle()).append(" (").append(movie.getGenre()).append("), ");
    				}
    			}
    		}
    		
    		String aiPrompt = watchlistSummary.toString() + 
    			"Based on these movies, recommend 5 popular/universal movies that are similar in genre or style. " +
    			"Format: Movie Title (Year) - Genre - Brief description. Use 🌟 emoji for each movie.";
    		
    		String aiResponse = callAIServiceWithRetry(aiPrompt);
    		return "**Popular movie recommendations from around the world:**\n\n" + aiResponse;
    	} catch (Exception e) {
    		log.error("Error getting AI recommendations: {}", e.getMessage(), e);
    		return "Sorry, I couldn't get AI recommendations at the moment. Error: " + e.getMessage();
    	}
    }
    
    private String callAIServiceWithRetry(String prompt) {
    	for (int attempt = 1; attempt <= 3; attempt++) {
    		try {
    			log.info("AI service call attempt {} with prompt: {}", attempt, prompt.substring(0, Math.min(100, prompt.length())));
    			
    			HttpHeaders headers = new HttpHeaders();
    			headers.setContentType(MediaType.APPLICATION_JSON);
    			
    			String requestBody = "{\"promptMessage\":\"" + prompt.replace("\"", "\\\"") + "\"}";
    			HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
    			
    			log.info("Calling AI service at http://localhost:8088/ai/chat");
    			String aiResponse = restTemplate.postForObject(
    				"http://localhost:8088/ai/chat", request, String.class);
    			
    			log.info("AI Response received successfully");
    			return aiResponse;
    		} catch (Exception e) {
    			log.error("AI service call attempt {} failed: {}", attempt, e.getMessage());
    			if (attempt < 3) {
    				try {
    					Thread.sleep(2000 * attempt); // Progressive delay: 2s, 4s
    				} catch (InterruptedException ie) {
    					Thread.currentThread().interrupt();
    					throw new RuntimeException("Request interrupted", ie);
    				}
    			}
    		}
    	}
    	throw new RuntimeException("AI service unavailable after multiple attempts");
    }
    
    private void sendErrorMessage(ChatMessage originalRequest, String errorText) {
    	ChatMessage errorMessage = ChatMessage.builder()
    		.type(MessageType.AI_RESPONSE)
    		.content(errorText)
    		.sender(ASSISTANT)
    		.recipient(originalRequest.getSender())
    		.build();
    	
    	messagingTemplate.convertAndSendToUser(
    		originalRequest.getSender(), QUEUE, errorMessage);
    	messagingTemplate.convertAndSendToUser(
    		originalRequest.getRecipient(), QUEUE, errorMessage);
    }

}
