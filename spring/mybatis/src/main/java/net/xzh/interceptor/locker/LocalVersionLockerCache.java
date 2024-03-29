/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 342252328@qq.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.xzh.interceptor.locker;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

public class LocalVersionLockerCache implements VersionLockerCache {
	
	private static final Log log = LogFactory.getLog(LocalVersionLockerCache.class);
	private ConcurrentHashMap<String, ConcurrentHashMap<VersionLockerCache.MethodSignature, VersionLocker>> caches = new ConcurrentHashMap<>();
	
	@Override
	public boolean containMethodSignature(MethodSignature vm) {
		String nameSpace = getNameSpace(vm);
		ConcurrentHashMap<VersionLockerCache.MethodSignature, VersionLocker> cache = caches.get(nameSpace);
		if(null == cache || cache.isEmpty()) {
			return false;
		}
		boolean containsMethodSignature = cache.containsKey(vm);
		if(containsMethodSignature && log.isDebugEnabled()) {
			log.debug("The method " + nameSpace + vm.getId() + "is hit in cache.");
		}
		return containsMethodSignature;
	}
	
	// 这里去掉synchronized或者重入锁，因为这里的操作满足幂等性
	// Here remove synchronized keyword or ReentrantLock, because it's a idempotent operation
	@Override
	public void cacheMethod(VersionLockerCache.MethodSignature vm, VersionLocker locker) {
		String nameSpace = getNameSpace(vm);
		ConcurrentHashMap<VersionLockerCache.MethodSignature, VersionLocker> cache = caches.get(nameSpace);
		if(null == cache || cache.isEmpty()) {
			cache = new ConcurrentHashMap<>();
			cache.put(vm, locker);
			caches.put(nameSpace, cache);
			if(log.isDebugEnabled()) {
				log.debug("Locker debug info ==> " + nameSpace + ": " + vm.getId() + " is cached.");
			}
		} else {
			cache.put(vm, locker);
		}
	}

	@Override
	public VersionLocker getVersionLocker(VersionLockerCache.MethodSignature vm) {
		String nameSpace = getNameSpace(vm);
		ConcurrentHashMap<VersionLockerCache.MethodSignature, VersionLocker> cache = caches.get(nameSpace);
		if(null == cache || cache.isEmpty()) {
			return null;
		}
		return cache.get(vm);
	}

	private String getNameSpace(VersionLockerCache.MethodSignature vm) {
		String id = vm.getId();
		int pos = id.lastIndexOf(".");
		return id.substring(0, pos);
	}

}
