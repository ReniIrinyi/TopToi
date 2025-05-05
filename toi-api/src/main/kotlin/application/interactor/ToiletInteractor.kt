package application.interactor

import adapter.persistence.database.repository.ToiletRepository
import application.dto.ToiletDTO

class ToiletInteractor(
    private val toiletRepository: ToiletRepository
) {
      suspend fun fetchToilets(lat: Double, lng: Double): List<ToiletDTO> {
        var toilets= toiletRepository.fetchToilets(lat, lng)
        return toilets.map { ToiletDTO.fromModel(it) }
    }

    fun createToilet(request: ToiletDTO, userEmail:String): ToiletDTO? {
        val toiletModel = toiletRepository.addToilet(request, userEmail)
        return toiletModel?.let { ToiletDTO.fromModel(it) }
        }

     fun fetchToiletById(toiletId: Int): ToiletDTO? {
         val toilet = toiletRepository.fetchToilet(toiletId)
         return toilet?.let { ToiletDTO.fromModel(it) }
     }
}
