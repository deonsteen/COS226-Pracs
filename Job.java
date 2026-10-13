
public class Job {

    private final long id;

    public Job(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Job{" + "id=" + id + '}';
    }
}
