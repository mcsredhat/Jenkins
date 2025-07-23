// Utility functions for Vue.js application

/**
 * Format a date to a readable string
 * @param {Date|string} date - The date to format
 * @param {string} locale - The locale to use (default: 'en-US')
 * @returns {string} Formatted date string
 */
export const formatDate = (date, locale = 'en-US') => {
    const dateObj = new Date(date)
    return dateObj.toLocaleDateString(locale, {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    })
}

/**
 * Format a date to a relative time string (e.g., "2 hours ago")
 * @param {Date|string} date - The date to format
 * @returns {string} Relative time string
 */
export const formatRelativeTime = (date) => {
    const now = new Date()
    const dateObj = new Date(date)
    const diffInSeconds = Math.floor((now - dateObj) / 1000)

    if (diffInSeconds < 60) return `${diffInSeconds} seconds ago`
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} minutes ago`
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} hours ago`
    if (diffInSeconds < 2592000) return `${Math.floor(diffInSeconds / 86400)} days ago`
    if (diffInSeconds < 31536000) return `${Math.floor(diffInSeconds / 2592000)} months ago`
    return `${Math.floor(diffInSeconds / 31536000)} years ago`
}

/**
 * Debounce function to limit how often a function can be called
 * @param {Function} func - The function to debounce
 * @param {number} wait - The number of milliseconds to delay
 * @returns {Function} Debounced function
 */
export const debounce = (func, wait) => {
    let timeout
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout)
            func(...args)
        }
        clearTimeout(timeout)
        timeout = setTimeout(later, wait)
    }
}

/**
 * Throttle function to limit how often a function can be called
 * @param {Function} func - The function to throttle
 * @param {number} limit - The number of milliseconds to limit
 * @returns {Function} Throttled function
 */
export const throttle = (func, limit) => {
    let inThrottle
    return function executedFunction(...args) {
        if (!inThrottle) {
            func.apply(this, args)
            inThrottle = true
            setTimeout(() => inThrottle = false, limit)
        }
    }
}

/**
 * Deep clone an object
 * @param {any} obj - The object to clone
 * @returns {any} Deep cloned object
 */
export const deepClone = (obj) => {
    if (obj === null || typeof obj !== 'object') return obj
    if (obj instanceof Date) return new Date(obj.getTime())
    if (obj instanceof Array) return obj.map(item => deepClone(item))
    if (typeof obj === 'object') {
        const clonedObj = {}
        for (const key in obj) {
            if (obj.hasOwnProperty(key)) {
                clonedObj[key] = deepClone(obj[key])
            }
        }
        return clonedObj
    }
}

/**
 * Generate a random ID string
 * @param {number} length - The length of the ID (default: 8)
 * @returns {string} Random ID string
 */
export const generateId = (length = 8) => {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    let result = ''
    for (let i = 0; i < length; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length))
    }
    return result
}

/**
 * Capitalize the first letter of a string
 * @param {string} str - The string to capitalize
 * @returns {string} Capitalized string
 */
export const capitalize = (str) => {
    if (!str) return ''
    return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase()
}

/**
 * Convert a string to kebab-case
 * @param {string} str - The string to convert
 * @returns {string} Kebab-case string
 */
export const toKebabCase = (str) => {
    return str
        .replace(/([a-z])([A-Z])/g, '$1-$2')
        .replace(/[\s_]+/g, '-')
        .toLowerCase()
}

/**
 * Convert a string to camelCase
 * @param {string} str - The string to convert
 * @returns {string} CamelCase string
 */
export const toCamelCase = (str) => {
    return str
        .replace(/(?:^\w|[A-Z]|\b\w)/g, (word, index) => {
            return index === 0 ? word.toLowerCase() : word.toUpperCase()
        })
        .replace(/\s+/g, '')
}

/**
 * Format a number with commas as thousands separators
 * @param {number} num - The number to format
 * @returns {string} Formatted number string
 */
export const formatNumber = (num) => {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * Format bytes to human readable size
 * @param {number} bytes - The number of bytes
 * @param {number} decimals - Number of decimal places (default: 2)
 * @returns {string} Formatted size string
 */
export const formatBytes = (bytes, decimals = 2) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const dm = decimals < 0 ? 0 : decimals
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i]
}

/**
 * Check if a value is empty (null, undefined, empty string, empty array, empty object)
 * @param {any} value - The value to check
 * @returns {boolean} True if empty, false otherwise
 */
export const isEmpty = (value) => {
    if (value === null || value === undefined) return true
    if (typeof value === 'string') return value.trim() === ''
    if (Array.isArray(value)) return value.length === 0
    if (typeof value === 'object') return Object.keys(value).length === 0
    return false
}

/**
 * Get a nested object property safely
 * @param {object} obj - The object to get the property from
 * @param {string} path - The path to the property (e.g., 'user.profile.name')
 * @param {any} defaultValue - Default value if property doesn't exist
 * @returns {any} The property value or default value
 */
export const getNestedProperty = (obj, path, defaultValue = null) => {
    const keys = path.split('.')
    let current = obj

    for (const key of keys) {
        if (current === null || current === undefined || !(key in current)) {
            return defaultValue
        }
        current = current[key]
    }

    return current
}

/**
 * Sleep/delay function
 * @param {number} ms - Milliseconds to sleep
 * @returns {Promise} Promise that resolves after the specified time
 */
export const sleep = (ms) => {
    return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * Validate email address
 * @param {string} email - Email to validate
 * @returns {boolean} True if valid email, false otherwise
 */
export const isValidEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return emailRegex.test(email)
}

/**
 * Validate URL
 * @param {string} url - URL to validate
 * @returns {boolean} True if valid URL, false otherwise
 */
export const isValidUrl = (url) => {
    try {
        new URL(url)
        return true
    } catch {
        return false
    }
}

/**
 * Local storage helpers with error handling
 */
export const storage = {
    get: (key, defaultValue = null) => {
        try {
            const item = localStorage.getItem(key)
            return item ? JSON.parse(item) : defaultValue
        } catch (error) {
            console.error('Error getting from localStorage:', error)
            return defaultValue
        }
    },

    set: (key, value) => {
        try {
            localStorage.setItem(key, JSON.stringify(value))
            return true
        } catch (error) {
            console.error('Error setting to localStorage:', error)
            return false
        }
    },

    remove: (key) => {
        try {
            localStorage.removeItem(key)
            return true
        } catch (error) {
            console.error('Error removing from localStorage:', error)
            return false
        }
    },

    clear: () => {
        try {
            localStorage.clear()
            return true
        } catch (error) {
            console.error('Error clearing localStorage:', error)
            return false
        }
    }
}

/**
 * HTTP status code checker
 * @param {number} status - HTTP status code
 * @returns {object} Object with boolean properties for different status types
 */
export const httpStatus = (status) => ({
    isSuccess: status >= 200 && status < 300,
    isRedirect: status >= 300 && status < 400,
    isClientError: status >= 400 && status < 500,
    isServerError: status >= 500 && status < 600,
    isError: status >= 400
})

// Default export with all utilities
export default {
    formatDate,
    formatRelativeTime,
    debounce,
    throttle,
    deepClone,
    generateId,
    capitalize,
    toKebabCase,
    toCamelCase,
    formatNumber,
    formatBytes,
    isEmpty,
    getNestedProperty,
    sleep,
    isValidEmail,
    isValidUrl,
    storage,
    httpStatus
}