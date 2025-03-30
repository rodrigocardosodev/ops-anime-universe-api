package com.devcorp.ops_anime_universe_api.domain.model

/** Modelo de domínio que representa uma resposta paginada */
data class PageResponse<T>(
        val content: List<T>,
        val page: Int,
        val size: Int,
        val totalElements: Long,
        val totalPages: Int
) {
  companion object {
    fun <T> of(content: List<T>, page: Int, size: Int, totalElements: Long): PageResponse<T> {
      val totalPages =
              when {
                totalElements == 0L -> 0
                size == 0 -> 1
                totalElements % size == 0L -> (totalElements / size).toInt()
                else -> (totalElements / size + 1).toInt()
              }
      return PageResponse(content, page, size, totalElements, totalPages)
    }
  }
}
