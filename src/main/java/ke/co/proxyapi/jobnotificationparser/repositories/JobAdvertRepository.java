package ke.co.proxyapi.jobnotificationparser.repositories;

import ke.co.proxyapi.jobnotificationparser.models.JobAdvertModel;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobAdvertRepository extends PagingAndSortingRepository<JobAdvertModel, Long>
{
	Optional<JobAdvertModel> findByUrlHash(String hash);

	void deleteAllByCreatedAtLessThan(Long createdAt);
}
