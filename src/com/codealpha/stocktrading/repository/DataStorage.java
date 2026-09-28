package com.codealpha.stocktrading.repository;

import com.codealpha.stocktrading.model.Stock;
import com.codealpha.stocktrading.model.User;

import java.util.Collection;

public interface DataStorage {


    boolean exists();


    void save(Collection<Stock> stocks, Collection<User> users);


    void load(MarketRepository marketRepository, UserRepository userRepository);


    String describeLocation();
}
