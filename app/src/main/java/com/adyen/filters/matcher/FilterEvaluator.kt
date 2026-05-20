package com.adyen.filters.matcher

import com.adyen.filters.model.Filter

class FilterEvaluator(private val matcher: GlobMatcher) {

    fun evaluate(filters: List<Filter>, changedFiles: List<String>): Map<String, Boolean> =
        filters.associate { filter ->
            filter.name to qualifies(filter, changedFiles)
        }

    private fun qualifies(filter: Filter, changedFiles: List<String>): Boolean =
        changedFiles.any { file ->
            filter.includes.any { matcher.matches(it, file) } &&
                    filter.excludes.none { matcher.matches(it, file) }
        }
}