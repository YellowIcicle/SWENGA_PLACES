package at.fh.swenga.places.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.codec.binary.Base64;

@Entity
@Table(name = "Recommendations")
public class RecommendationModel {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(nullable = false, length = 140)
	private String title;

	@ManyToOne
	private PlaceModel place;

	@Column(length = 500)
	private String description;

	@ManyToOne(cascade = CascadeType.PERSIST)
	private UserModel user;

	private boolean approved;

	@OneToOne(cascade = CascadeType.ALL)
	private PictureModel recommendationPicture;

	private int rating;

	@Column(length = 30)
	private String season;

	@ManyToMany(mappedBy = "favRecommendations")
	private Set<UserModel> favUsers;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public PlaceModel getPlace() {
		return place;
	}

	public void setPlace(PlaceModel place) {
		this.place = place;
	}

	public Set<UserModel> getFavUsers() {
		return favUsers;
	}

	public void setFavUsers(Set<UserModel> favUsers) {
		this.favUsers = favUsers;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public UserModel getUser() {
		return user;
	}

	public void setUser(UserModel user) {
		this.user = user;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getSeason() {
		return season;
	}

	public void setSeason(String season) {
		this.season = season;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + id;
		result = prime * result + ((place == null) ? 0 : place.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecommendationModel other = (RecommendationModel) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (place == null) {
			if (other.place != null)
				return false;
		} else if (!place.equals(other.place))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	public RecommendationModel(String title, PlaceModel place, String description, UserModel user,
			PictureModel recommendationPicture, String season) {
		super();
		this.title = title;
		this.place = place;
		this.description = description;
		this.user = user;
		this.recommendationPicture = recommendationPicture;
		this.season = season;
	}

	@Override
	public String toString() {
		return "RecommendationModel [id=" + id + ", title=" + title + ", place=" + place + ", description="
				+ description + ", user=" + user + ", approved=" + approved + ", recommendationPicture="
				+ recommendationPicture + ", rating=" + rating + ", season=" + season + ", favUsers=" + favUsers + "]";
	}

	public PictureModel getRecommendationPicture() {
		return recommendationPicture;
	}

	public void setRecommendationPicture(PictureModel recommendationPicture) {
		this.recommendationPicture = recommendationPicture;
	}

	public RecommendationModel() {
		super();
	}

	public RecommendationModel(String title, PlaceModel place, String description, UserModel user) {
		super();
		this.title = title;
		this.place = place;
		this.description = description;
		this.user = user;
	}

	public RecommendationModel(String title, PlaceModel place, UserModel user, boolean approved, int rating) {
		super();
		this.title = title;
		this.place = place;
		this.user = user;
		this.approved = approved;
		this.rating = rating;
	}

	/**
	 * @return Base64-representation of recommendationpicture or default picture if
	 *         none present
	 */
	public String getPicture() {
		if (recommendationPicture == null) {
			return "bootstrap/img/globe.jpg";
		} else {
			try {
				return "data:image/jpg;base64," + new String(Base64.encodeBase64(recommendationPicture.getContent()));
			} catch (Exception e) {
				return "bootstrap/img/globe.jpg";
			}
		}
	}

}
