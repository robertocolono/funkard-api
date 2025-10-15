package com.funkard.admin.dto;

public class AdminStatsDTO {
    public long users;
    public long cards;
    public long pending;
    public long tickets;

    // Constructors
    public AdminStatsDTO() {}

    public AdminStatsDTO(long users, long cards, long pending, long tickets) {
        this.users = users;
        this.cards = cards;
        this.pending = pending;
        this.tickets = tickets;
    }

    // Getters and Setters
    public long getUsers() { return users; }
    public void setUsers(long users) { this.users = users; }

    public long getCards() { return cards; }
    public void setCards(long cards) { this.cards = cards; }

    public long getPending() { return pending; }
    public void setPending(long pending) { this.pending = pending; }

    public long getTickets() { return tickets; }
    public void setTickets(long tickets) { this.tickets = tickets; }
}
