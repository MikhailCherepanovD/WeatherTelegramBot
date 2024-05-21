package ru.spring.core.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.spring.core.project.entity.Place;
@Repository
public interface PlaceRepository extends JpaRepository<Place,Long> {
    //По аннотации @Query можем использовать напрямую SQL запрос

  /*  @Query(value ="SELECT * FROM PLACE WHERE latitude = ?1 AND longitude =?2",nativeQuery = true)
    Place findByLongitudeAndLatitude(float latitude,float longitude);
    Place FindByPlaceName(String placeName);*/

}
