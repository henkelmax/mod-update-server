package utils

import (
	"sync"
	"time"
)

type cacheItem[T any] struct {
	value     T
	expiresAt time.Time
}

type Cache[T any] struct {
	mutex sync.RWMutex
	items map[string]cacheItem[T]
	ttl   time.Duration
}

func NewCache[T any](ttl time.Duration) *Cache[T] {
	return &Cache[T]{
		items: make(map[string]cacheItem[T]),
		ttl:   ttl,
	}
}

func (c *Cache[T]) Set(key string, value T) {
	c.mutex.Lock()
	defer c.mutex.Unlock()

	c.items[key] = cacheItem[T]{
		value:     value,
		expiresAt: time.Now().Add(c.ttl),
	}
}

func (c *Cache[T]) Get(key string) *T {
	c.mutex.RLock()
	item, exists := c.items[key]
	c.mutex.RUnlock()

	if !exists {
		return nil
	}

	if !time.Now().After(item.expiresAt) {
		return &item.value
	}

	c.mutex.Lock()
	defer c.mutex.Unlock()

	currentItem, exists := c.items[key]
	if exists && time.Now().After(currentItem.expiresAt) {
		delete(c.items, key)
	}

	return nil
}
